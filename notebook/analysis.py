#!/usr/bin/python3

# %%
import json
import pandas as pd
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt
import seaborn as sns
from IPython.display import display

with open("./data/logs/log.json") as file:
    jsonFile = json.load(file)['experiments']

experiments = pd.DataFrame(jsonFile)
display(experiments.head())

# %%
sns.set_style("white", {
    'font.family': [u'serif'],
    'font.serif': [u'Computer Modern'] })
sns.set_palette("muted", desat=0.6)
mpl.rcParams['text.usetex'] = True

# %%
labels = [r'$2^{' + str(i) + r'}$' for i in range(1, 17)]
boxplot_args = {
    # 'labels': labels,
    'saturation': 1,
    'sym': '',
    'whis': [1, 99],
    'showmeans': True,
    'meanline': True,
    'linewidth': 1,
    'boxprops': {
        'edgecolor': '#262626'
    },
    'capprops': {
        'color': '#262626',
    },
    'whiskerprops': {
        'markerfacecolor': '#262626',
        'markeredgecolor': '#262626',
        'color': '#262626'
    },
    'flierprops': {
        'color': '#262626'
    },
    'meanprops': {
        'color': '#262626',
        'linewidth': 1,
        'linestyle': ':'
    },
    'medianprops': {
        'color': '#262626'
    }
}

# %%
def unfold(hist):
    if type(hist) is dict:
        return [ int(k) for k, v in hist.items() for i in range(v) ]

    if type(hist) is list:
        return [ k for k in range(len(hist)) for i in range(int(hist[k])) ]

    return [ k for k, v in hist for i in range(int(v)) ]


# %%
metrics = pd.read_csv("data/cytoscape/summary/stats.csv", sep="\t").sort_values('nodes')
metrics.index = range(1, 16)
display(metrics)

# %%
keys = pd.DataFrame()
lookups = pd.DataFrame()
pathLengths = pd.DataFrame()

for i, exp in experiments.iterrows():
    df = pd.DataFrame(unfold(exp['gaps']), columns=['Keys'])
    df['Nodes'] = exp['nodes']
    df['Type'] = "Assigned"
    keys = keys.append(df)
    df = pd.DataFrame(unfold(exp['endNodes']), columns=['Keys'])
    df['Nodes'] = exp['nodes']
    df['Type'] = "Queried"
    keys = keys.append(df)
    df = pd.DataFrame(unfold(exp['queries']), columns=["Lookups"])
    df['Nodes'] = exp['nodes']
    lookups = lookups.append(df)
    df = pd.DataFrame(unfold(exp['pathLengths']), columns=["Path Length"])
    df['Nodes'] = exp['nodes']
    df['Type'] = "Chord"
    pathLengths = pathLengths.append(df)

display(keys.head())
display(lookups.head())
display(pathLengths.head())

# %%
degrees = pd.DataFrame()

for exp in range(2, 17):
    nodes = 2**exp
    ins = unfold(np.genfromtxt("data/cytoscape/degree/single_{}_in.csv".format(nodes)))
    inm = unfold(np.genfromtxt("data/cytoscape/degree/multi_{}_in.csv".format(nodes)))
    outs = unfold(np.genfromtxt("data/cytoscape/degree/single_{}_in.csv".format(nodes)))
    spl = unfold([1] + (np.genfromtxt("data/cytoscape/shorthestpath/multi_{}_spl.csv".format(nodes))[:, 1]/float(nodes)).tolist())
    df = pd.DataFrame(ins, columns=["Degree"])
    df['Nodes'] = nodes
    df['Type'] = 'In (Graph)'
    degrees = degrees.append(df)
    df = pd.DataFrame(outs, columns=["Degree"])
    df['Nodes'] = nodes
    df['Type'] = 'Out (Graph)'
    degrees = degrees.append(df)
    df = pd.DataFrame(inm, columns=["Degree"])
    df['Nodes'] = nodes
    df['Type'] = 'In (Multigraph)'
    degrees = degrees.append(df)
    df = pd.DataFrame(spl, columns=["Path Length"])
    df['Nodes'] = nodes
    df['Type'] = "Shorthest Path"
    pathLengths = pathLengths.append(df)

display(degrees.head())
display(pathLengths.head())


# %% --- GAPS/KEYS PER NODE --- %% #
sns.boxplot(data=keys[keys['Type'] == "Assigned"], x="Nodes", y="Keys", hue="Type", width=.5, **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend().set_visible(False)
plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/dist_keys.pdf")
plt.show()

# %%
sns.boxplot(data=keys[keys['Type'] == "Queried"], x="Nodes", y="Keys", hue="Type", width=.5, **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend().set_visible(False)
# plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/dist_end_nodes.pdf")
plt.show()

# %%
assigned = keys[(keys['Nodes'] == 2**12) & (keys["Type"] == "Assigned")]["Keys"]
queried = keys[(keys['Nodes'] == 2**12) & (keys["Type"] == "Queried")]["Keys"]
v_pdf, v_keys = np.histogram(assigned, bins=max(assigned), density=True)
r_pdf, r_keys = np.histogram(queried, bins=max(queried), density=True)
fix, ax = plt.subplots()
ax.plot(v_keys[:-1], v_pdf, lw=1.5, label="Assigned")
ax.plot(r_keys[:-1], r_pdf, lw=1.5, label="Queried")
ax.legend().set_visible(True)
ax.set_xlabel("Keys")
ax.set_ylabel("PDF")
plt.tight_layout()
plt.savefig("report/figures/pdf_keys.pdf")
plt.show()


# %% --- PATH LENGTH --- %% #
sns.boxplot(data=pathLengths, x="Nodes", y="Path Length", hue="Type", **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.tight_layout()
plt.savefig("report/figures/dist_path_length.pdf")
plt.show()

# %%
chord = pathLengths[(pathLengths['Nodes'] == 2**12) & (pathLengths["Type"] == "Chord")]["Path Length"]
sp = pathLengths[(pathLengths['Nodes'] == 2**12) & (pathLengths["Type"] == "Shorthest Path")]["Path Length"]
c_pdf, c_keys = np.histogram(chord, bins=max(chord), density=True)
sp_pdf, sp_keys = np.histogram(sp, bins=max(sp), density=True)
fix, ax = plt.subplots()
ax.plot(c_keys[:-1], c_pdf, lw=1.5, label="Chord")
ax.plot(sp_keys[:-1], sp_pdf, lw=1.5, label="Shorthest Path")
ax.legend().set_visible(True)
ax.set_xlabel("Path Length")
ax.set_ylabel("PDF")
plt.tight_layout()
plt.savefig("report/figures/pdf_path_length.pdf")
plt.show()


# %% --- QUERIES --- %% #
sns.boxplot(data=lookups, x="Nodes", y="Lookups", color=sns.color_palette()[0], width=.5, **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.tight_layout()
plt.savefig("report/figures/dist_lookups.pdf")
plt.show()

# %%
q = lookups[lookups['Nodes'] == 2**12]["Lookups"]
q_pdf, q_keys = np.histogram(q, bins=max(q), density=True)
fix, ax = plt.subplots()
ax.plot(q_keys[:-1], q_pdf, lw=1.5, label="Lookups")
ax.legend().set_visible(False)
ax.set_xlabel("Lookups")
ax.set_ylabel("PDF")
plt.tight_layout()
plt.savefig("report/figures/pdf_lookups.pdf")
plt.show()

# %% --- METRICS --- %% #
fig, ax = plt.subplots()
ax.plot(metrics["radius"], label="Radius")
ax.plot(metrics["diameter"], label="Diameter")
ax.legend().set_visible(True)
ax.set_xticklabels(labels[1:])
ax.set_xlabel("Nodes")
ax.set_ylabel("Path Length")
plt.tight_layout()
plt.savefig("report/figures/metrics_rd.pdf")
plt.show()

# %%
fig, ax = plt.subplots()
ax.plot(metrics["cc"], label="Clustering Coefficient")
ax.legend().set_visible(False)
ax.set_xticklabels(labels[1:])
ax.set_xlabel("Nodes")
ax.set_ylabel("Clustering Coefficient")
plt.tight_layout()
plt.savefig("report/figures/metrics_cc.pdf")
plt.show()

# %% --- NODE DEGREES --- %% #
sns.boxplot(data=degrees, x="Nodes", y="Degree", hue="Type", **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.tight_layout()
plt.savefig("report/figures/dist_degrees.pdf")
plt.show()
