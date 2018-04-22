#!/usr/bin/python3

# %%
import json
import pandas as pd
import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt
import seaborn as sns
from IPython.display import display

# %%
def unfold(hist):
    if type(hist) is dict:
        return [ int(k) for k, v in hist.items() for i in range(v) ]

    if type(hist) is list:
        return [ k for k in range(len(hist)) for i in range(int(hist[k])) ]

    return [ k for k, v in hist for i in range(int(v)) ]


# %% --- SETTINGS --- %% #
sns.set(font_scale=1.5)
sns.set_style("white")
sns.set_palette(['w', 'gray'])
mpl.rcParams['text.usetex'] = True
# mpl.rcParams['font.size'] = 16
mpl.rcParams['font.family'] = [u'serif']
mpl.rcParams['font.serif'] = [u'Computer Modern']

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
        'color': '#262626'
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


# %% --- DATA PREPARATION --- %% #
with open("./data/logs/log.json") as file:
    jsonFile = json.load(file)['experiments']

experiments = pd.DataFrame(jsonFile)
display(experiments.head())

# %%
metrics = pd.read_csv("data/cytoscape/summary/stats.csv", sep="\t").sort_values('nodes')
metrics["Expected"] = 2./np.log2(metrics["nodes"])
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
    spl = unfold([1] + (np.genfromtxt("data/cytoscape/shorthestpath/multi_{}_spl.csv".format(nodes))[:, 1]/float(nodes)).tolist())
    df = pd.DataFrame(ins, columns=["Indegree"])
    df['Nodes'] = nodes
    df['Type'] = 'Graph'
    degrees = degrees.append(df)
    degrees = degrees.append(df)
    df = pd.DataFrame(inm, columns=["Indegree"])
    df['Nodes'] = nodes
    df['Type'] = 'Multigraph'
    degrees = degrees.append(df)
    df = pd.DataFrame(spl, columns=["Path Length"])
    df['Nodes'] = nodes
    df['Type'] = "Shorthest Path"
    pathLengths = pathLengths.append(df)

display(degrees.head())
display(pathLengths.head())


# %% --- GAPS/KEYS PER NODE --- %% #
sns.boxplot(data=keys, x="Nodes", y="Keys", hue="Type",  **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/dist_keys.pdf", bbox_inches='tight')
plt.show()

# %%
assigned = keys[(keys['Nodes'] == 2**12) & (keys["Type"] == "Assigned")]["Keys"]
queried = keys[(keys['Nodes'] == 2**12) & (keys["Type"] == "Queried")]["Keys"]
v_pdf, v_keys = np.histogram(assigned, bins=max(assigned), density=True)
r_pdf, r_keys = np.histogram(queried, bins=max(queried), density=True)
fix, ax = plt.subplots()
ax.plot(v_keys[:-1], v_pdf, lw=1, label="Assigned", c='k')
ax.plot(r_keys[:-1], r_pdf, lw=1, label="Queried", c='k', linestyle='--')
ax.legend().set_visible(True)
ax.set_xlabel("Keys")
ax.set_ylabel("PDF")
# plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/pdf_keys.pdf", bbox_inches='tight')
plt.show()


# %% --- PATH LENGTH --- %% #
sns.boxplot(data=pathLengths, x="Nodes", y="Path Length", hue="Type", **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.tight_layout()
plt.savefig("report/figures/dist_path_lengths.pdf", bbox_inches='tight')
plt.show()

# %%
chord = pathLengths[(pathLengths['Nodes'] == 2**12) & (pathLengths["Type"] == "Chord")]["Path Length"]
sp = pathLengths[(pathLengths['Nodes'] == 2**12) & (pathLengths["Type"] == "Shorthest Path")]["Path Length"]
c_pdf, c_keys = np.histogram(chord, bins=max(chord), density=True)
sp_pdf, sp_keys = np.histogram(sp, bins=max(sp), density=True)
fix, ax = plt.subplots()
ax.plot(c_keys[:-1], c_pdf, lw=1, label="Chord", c='k')
ax.plot(c_keys[:-1], sp_pdf.tolist() + [0, 0, 0, 0], lw=1, label="Shorthest Path", c='k', linestyle='--')
ax.legend().set_visible(True)
ax.set_xlabel("Path Length")
ax.set_ylabel("PDF")
plt.tight_layout()
plt.savefig("report/figures/pdf_path_lengths.pdf", bbox_inches='tight')
plt.show()


# %% --- QUERIES --- %% #
sns.boxplot(data=lookups, x="Nodes", y="Lookups", color=sns.color_palette()[0], width=.5, **boxplot_args)
plt.gca().set_xticklabels(labels)
plt.gca().legend(title=None)
plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/dist_lookups.pdf", bbox_inches='tight')
plt.show()

# %%
q = lookups[lookups['Nodes'] == 2**12]["Lookups"]
q_pdf, q_keys = np.histogram(q, bins=max(q), density=True)
fix, ax = plt.subplots()
ax.plot(q_keys[:-1], q_pdf, lw=1, label="Lookups", c='k')
ax.legend().set_visible(False)
ax.set_xlabel("Lookups")
ax.set_ylabel("PDF")
plt.tight_layout()
plt.savefig("report/figures/pdf_lookups.pdf", bbox_inches='tight')
plt.show()


# %% --- METRICS --- %% #
fig, ax = plt.subplots()
ax = metrics.plot(x="nodes", y="radius", lw=1, label="Radius", c='k')
metrics.plot(x="nodes", y="diameter", lw=1, label="Diameter", c='k', linestyle='--', ax=ax)
ax.legend().set_visible(True)
ax.set_xlabel("Nodes")
ax.set_ylabel("Path Length")
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/metrics_rd.pdf", bbox_inches='tight')
plt.show()

# %%
fig, ax = plt.subplots()

ax = metrics.plot(x="nodes", y="cc", lw=1, label="Observed", c='k')
metrics.plot(x="nodes", y="Expected", lw=1, c='k', linestyle='--', ax=ax)
ax.legend().set_visible(True)
ax.set_xlabel("Nodes")
ax.set_ylabel("Clustering Coefficient")
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.tight_layout()
plt.savefig("report/figures/metrics_cc.pdf", bbox_inches='tight')
plt.show()


# %% --- NODE DEGREES --- %% #
sns.boxplot(data=degrees, x="Nodes", y="Indegree", hue="Type", **boxplot_args)
plt.gca().set_xticklabels(labels[1:])
plt.gca().legend(title=None)
plt.tight_layout()
plt.savefig("report/figures/dist_degrees.pdf", bbox_inches='tight')
plt.show()
