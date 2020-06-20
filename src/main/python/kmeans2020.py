# -*- coding: utf-8 -*-

import os
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler

plt.rcParams['figure.figsize'] = (16, 9)
plt.style.use('ggplot')

os.chdir(r'D:\Programación\tfm\src\main\data')

df = pd.read_csv('mainDf2020.csv')
df.columns = ['date', 'origin', 'weight', 'change']
df.index = df['date']
df = df.drop(['date', 'origin'], axis=1).dropna()

print(df)

scaler = StandardScaler()
scaler.fit(df)
scaled_df = scaler.transform(df)
scaled_df = pd.DataFrame(scaled_df, columns=df.columns)
scaled_df.index = df.index

# nc = range(1, 20)
# KNN = [KMeans(algorithm='auto', copy_x=True, init='k-means++', max_iter=300, n_clusters=4, n_init=10, random_state=None, tol=0.0001, verbose=0) for i in nc]
# score = [KNN[i].fit(scaled_df).score(scaled_df)
# for i in range(len(KNN))]
#
# plt.plot(nc, score)
# plt.xlabel('Number of Clusters')
# plt.ylabel('Score')
# plt.title('Elbow Curve')
# plt.show()

KNN = KMeans(algorithm='auto', copy_x=True, init='k-means++', max_iter=300, n_clusters=2, n_init=10, random_state=None,
             tol=0.0001, verbose=0)

KNN.fit(df)

centers = KNN.cluster_centers_
centers = pd.DataFrame(centers, columns=scaled_df.columns)
print(centers)

# Getting the values and plotting it
f1 = df['weight'].values
f2 = df['change'].values

labels = KNN.predict(df)
colors = ['red', 'green']
assign = []
for row in labels:
    assign.append(colors[row])

plt.scatter(f1, f2, c=assign, s=70)
plt.show()

# print("Homogeneity: %0.3f" % metrics.homogeneity_score(labels, KNN.labels_))
# print("Completeness: %0.3f" % metrics.completeness_score(labels, KNN.labels_))
# print("V-measure: %0.3f" % metrics.v_measure_score(labels, KNN.labels_))
# print("Adjusted Rand-Index: %.3f"
#       % metrics.adjusted_rand_score(labels, KNN.labels_))
# print("Silhouette Coefficient: %0.3f"
#       % metrics.silhouette_score(X, KNN.labels_, sample_size=1000))
#
# print()