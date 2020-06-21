# -*- coding: utf-8 -*-

import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import statsmodels.api as sm
from pylab import rcParams
import itertools
import pickle

os.chdir(r'D:\Programación\tfm\src\main\data')

# Read a df with columns 'date', 'origin', 'weight', 'change'
df = pd.read_csv('mainDf2020.csv')

# Weight is the sentiment score and change is the change in the Ibex 35 index.
df.columns = ['date', 'origin', 'weight', 'change']
df['date'] = pd.to_datetime(df['date'])

df = df.set_index('date').dropna()

serie = df['change']

rcParams['figure.figsize'] = 18, 8

decomposition = sm.tsa.seasonal_decompose(serie, model='additive')
fig = decomposition.plot()
plt.show()

p = d = q = range(0, 4)
pdq = list(itertools.product(p, d, q))
seasonal_pdq = [(x[0], x[1], x[2], 6) for x in list(itertools.product(p, d, q))]

'''
Grid search para encontrar el mejor modelo arima.
'''
bests = {}
for param in pdq:
    for param_seasonal in seasonal_pdq:
        try:
            mod = sm.tsa.statespace.SARIMAX(serie,
                                            order=param,
                                            seasonal_order=param_seasonal,
                                            enforce_stationarity=False,
                                            enforce_invertibility=False)
            results = mod.fit()
            print('ARIMA{}x{} - AIC:{}'.format(param, param_seasonal, results.aic))
            ind = (param, param_seasonal)
            bests[ind] = results.aic
        except Exception as e:
            print(type(e), e)
            continue

print(min(bests.items(), key=lambda x: x[1]))
# =============================================================================
# Mejor modelo con seasonality en 6 meses. (1,1,1)x(0,1,1)6 AIC : 2392.5135
# (((0, 1, 3), (1, 1, 3, 6)) --> Mejor modelo con 3 periodos
# =============================================================================
mod0 = sm.tsa.statespace.SARIMAX(serie,
                                order=(1, 1, 1),
                                seasonal_order=(0, 1, 1, 6),
                                enforce_stationarity=False,
                                enforce_invertibility=False)

results = mod0.fit()

print(results.summary().tables[1])

results.plot_diagnostics(figsize=(16, 8))
plt.show()

#Veamos la prediccion

pred = results.get_prediction()
pred_ic = pred.conf_int()
pred_ic['lower UNIDADES'][:'2018'] = serie[:'2018']
pred_ic['upper UNIDADES'][:'2018'] = serie[:'2018']
ax = serie.plot(label='Real')
pred.predicted_mean['2019':].plot(
        ax=ax, label='Prediccion a un periodo', alpha=.7, figsize=(14, 7))

ax.fill_between(pred_ic.index,
                pred_ic.iloc[:, 0],
                pred_ic.iloc[:, 1], color='k', alpha=.2)

ax.set_ylabel('Unidades')
plt.legend()

plt.show()

y_pred = pred.predicted_mean['2019':]
y_v = serie['2019':]

mse = ((y_pred - y_v) ** 2).mean()
print('El error cuadrático medio de la predicción es: {}'.format(round(mse, 2)))
print('Y su raiz cuadrada es {}'.format(round(np.sqrt(mse), 2)))
# =============================================================================
# Mejor modelo con seasonality en 12 meses. (1,1,2)x(0,2,2)12 AIC : 
# =============================================================================
mod = sm.tsa.statespace.SARIMAX(serie,
                                order=(1, 1, 2),
                                seasonal_order=(0, 2, 2, 12),
                                enforce_stationarity=False,
                                enforce_invertibility=False)

results = mod.fit()

print(results.summary().tables[1])

results.plot_diagnostics(figsize=(16, 8))
plt.show()

#Veamos la prediccion

pred = results.get_prediction()
pred_ic = pred.conf_int()
pred_ic['lower UNIDADES'][:'2018'] = serie[:'2018']
pred_ic['upper UNIDADES'][:'2018'] = serie[:'2018']
ax = serie.plot(label='Real')
pred.predicted_mean['2019':].plot(
        ax=ax, label='Prediccion a un periodo', alpha=.7, figsize=(14, 7))

ax.fill_between(pred_ic.index,
                pred_ic.iloc[:, 0],
                pred_ic.iloc[:, 1], color='k', alpha=.2)

ax.set_ylabel('Unidades')
plt.legend()

plt.show()

y_pred = pred.predicted_mean['2019':]
y_v = serie['2019':]

mse = ((y_pred - y_v) ** 2).mean()
print('El error cuadrático medio de la predicción es: {}'.format(round(mse, 2)))
print('Y su raiz cuadrada es {}'.format(round(np.sqrt(mse), 2)))

# =============================================================================
# El mejor modelo tiene seasonality 6 porque las compras aumentan en verano e
# invierno debido a las rebajas aunque presente un AIC más bajo es bastante más
# preciso que el modelo con seasonality 12. Utilizamos este para mostrar predicciones
# =============================================================================

pred_uc = results.get_forecast(steps=25)
pred_ic = pred_uc.conf_int()

ax = serie.plot(label='Real', figsize=(14, 7))
pred_uc.predicted_mean.plot(ax=ax, label='Forecast')
ax.fill_between(pred_ic.index,
                pred_ic.iloc[:, 0],
                pred_ic.iloc[:, 1], color='k', alpha=.25)
ax.set_xlabel('Fecha')
ax.set_ylabel('Unidades')

plt.legend()
plt.show()

# =============================================================================
# Ajuste natalidad con (((0, 1, 3), (1, 1, 3, 6))
# =============================================================================

mod = sm.tsa.statespace.SARIMAX(serie,
                                order=(0, 1, 3),
                                seasonal_order=(1, 1, 3, 6),
                                enforce_stationarity=False,
                                enforce_invertibility=False)

results = mod.fit()

prediction = results.get_forecast(18).predicted_mean
df_natalidad = serie.append(prediction)
df_natalidad.to_csv('nat.csv', sep=';')