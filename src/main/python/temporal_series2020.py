# -- coding: utf-8 --

import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import statsmodels.api as sm
from pylab import rcParams
import itertools
from statsmodels.tsa.stattools import adfuller

os.chdir(r'D:\Programación\tfm\src\main\data')

df = pd.read_csv('mainDf2020_filled_na.csv',
                 header=0,
                 names=['date', 'weight', 'change'])

df['date'] = pd.to_datetime(df.date, format='%Y%m%d')
df.index = df.date
series = df.change

rcParams['figure.figsize'] = 18, 8

serie2 = series.resample('W').mean()
serie2.fillna(serie2.mean())
serie2.fillna(serie2.mean(), inplace=True)

decomposition = sm.tsa.seasonal_decompose(serie2, model='additive')
fig = decomposition.plot()
plt.show()

# =============================================================================
# Check Stationarity
# =============================================================================

result = adfuller(serie2)
print('ADF Statistic: %f' % result[0])
print('p-value: %f' % result[1])
print('Critical Values:')
for key, value in result[4].items():
    print('\t%s: %.3f' % (key, value))

# La serie no tiene una raiz única, rechazamos H0 y la serie es ESTACIONARIA
# no tiene una estructura dependiente del tiempo

p = d = q = range(0, 3)
pdq = list(itertools.product(p, d, q))
seasonal_pdq = [(x[0], x[1], x[2], 7) for x in list(itertools.product(p, d, q))]

'''
Grid search para encontrar el mejor modelo arima.
'''
bests = {}
for param in pdq:
    for param_seasonal in seasonal_pdq:
        try:
            mod = sm.tsa.statespace.SARIMAX(serie2,
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
# Mejor modelo con seasonality en 7 dias.
# (((0, 0, 2), (1, 1, 2, 7)) AIC : 189.016994
# =============================================================================
mod0 = sm.tsa.statespace.SARIMAX(serie2,
                                 order=(0, 0, 2),
                                 seasonal_order=(1, 1, 2, 7),
                                 enforce_stationarity=False,
                                 enforce_invertibility=False)

results = mod0.fit()

print(results.summary().tables[1])

results.plot_diagnostics(figsize=(16, 8))
plt.show()

# Veamos la prediccion

pred = results.get_prediction()

fecha_ini = '2019-01'  # enero 2020
y_pred = pred.predicted_mean[fecha_ini:].to_frame('pred')
y_pred['real'] = serie2
y_pred['error'] = (y_pred.real - y_pred.pred)

print('El error cuadrático medio de la predicción es: {}'.format(round(y_pred.mse.mean(), 2)))
print('Y su raiz cuadrada es {}'.format(round(np.sqrt(y_pred.mse.mean()), 2)))
