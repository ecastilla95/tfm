# -*- coding: utf-8 -*-

import os
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.cluster import MiniBatchKMeans
from sklearn.preprocessing import StandardScaler

plt.rcParams['figure.figsize'] = (16, 9)
plt.style.use('ggplot')

os.chdir(r'D:\Programación\tfm\src\main\data')

df = pd.read_csv('mainDf2020.csv')
df.columns = ['date', 'origin', 'weight', 'change']
df.index = df['date']
df = df.drop(['date', 'origin'], axis=1).dropna()


def cluster_elbow_method(df, top):
    distances = []
    K = range(1, top)
    for k in K:
        km = MiniBatchKMeans(n_clusters=k)
        km = km.fit(df)
        distances.append(km.inertia_)

    plt.plot(K, distances, 'bx-')
    plt.xlabel('k')
    plt.ylabel('error')
    plt.title('Elbow Method For Optimal k')
    plt.show()


cluster_elbow_method(df, 20)

colors = ['red', 'green', 'blue', 'yellow', 'pink']
for number_of_clusters in range(2, 6):

    KNN = MiniBatchKMeans(n_clusters=number_of_clusters)
    KNN.fit(df)

    centers = KNN.cluster_centers_
    centers = pd.DataFrame(centers, columns=df.columns)
    print(centers)

    # Getting the values and plotting it
    f1 = df['weight'].values
    f2 = df['change'].values

    labels = KNN.predict(df)
    assign = []
    for row in labels:
        assign.append(colors[row])

    plt.scatter(f1, f2, c=assign, s=70)
    plt.show()
