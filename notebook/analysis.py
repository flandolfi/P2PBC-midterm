# %%
import json
import pandas as pd
import numpy as np
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
    'font.serif': [u'Latin Modern'],
    'axes.linewidth': 1.0})
sns.color_palette("muted")

# %%
def unfold(dictionary):
    return [ int(k) for k, v in dictionary.items() for i in range(v) ]

# %%
gaps = pd.DataFrame()
endNodes = pd.DataFrame()
queries = pd.DataFrame()
pathLengths = pd.DataFrame()

for i, exp in experiments.iterrows():
    df = pd.DataFrame(unfold(exp['gaps'])).describe(percentiles=[.01, .99]).transpose()
    df['nodes'] = exp['nodes']
    gaps = gaps.append(df)
    df = pd.DataFrame(unfold(exp['endNodes'])).describe(percentiles=[.01, .99]).transpose()
    df['nodes'] = exp['nodes']
    endNodes = endNodes.append(df)
    df = pd.DataFrame(unfold(exp['queries'])).describe(percentiles=[.01, .99]).transpose()
    df['nodes'] = exp['nodes']
    queries = queries.append(df)
    df = pd.DataFrame(unfold(exp['pathLengths'])).describe(percentiles=[.01, .99]).transpose()
    df['nodes'] = exp['nodes']
    pathLengths = pathLengths.append(df)

display(gaps)
display(endNodes)
display(queries)
display(pathLengths)

# %% --- GAPS/KEYS PER NODE --- %% #
yerrh=(gaps['mean'].as_matrix() - gaps['1%'].as_matrix()).T
yerrl=(gaps['99%'].as_matrix() - gaps['mean'].as_matrix()).T
plt.errorbar(x=gaps['nodes'], y=gaps['mean'], yerr=[yerrh, yerrl],
    fmt='.-.',
    capsize=5,
    elinewidth=1,
    markeredgewidth=1,
    linewidth=1)
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.show()

# %%
sns.distplot(unfold(experiments.iloc[11]['gaps']), kde=False)
plt.show()

# %%
g_dict = experiments.iloc[11]['gaps']
g_keys = sorted([int(k) for k in g_dict.keys()])
g_pdf = [g_dict['{}'.format(k)]/float(experiments.iloc[11]['iterations']*(2**12))
        for k in g_keys]
plt.plot(g_keys, g_pdf, lw=1)
plt.show()

# %% --- END NODES --- %% #
yerrh=(endNodes['mean'].as_matrix() - endNodes['1%'].as_matrix()).T
yerrl=(endNodes['99%'].as_matrix() - endNodes['mean'].as_matrix()).T
plt.errorbar(x=endNodes['nodes'], y=endNodes['mean'], yerr=[yerrh, yerrl],
    fmt='.-.',
    capsize=5,
    elinewidth=1,
    markeredgewidth=1,
    linewidth=1)
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.show()

# %%
sns.distplot(unfold(experiments.iloc[11]['endNodes']), kde=False)
plt.show()

# %%
en_dict = experiments.iloc[11]['endNodes']
en_keys = sorted([int(k) for k in en_dict.keys()])
en_pdf = [en_dict['{}'.format(k)]/float(experiments.iloc[11]['iterations']*(2**12))
        for k in en_keys]
plt.plot(en_keys, en_pdf, lw=1)
plt.show()

# %%
ax = plt.axes()
ax.plot(en_keys, en_pdf, lw=1)
ax.plot(g_keys, g_pdf, lw=1, c=sns.color_palette()[3])
plt.show()

# %% --- PATH LENGTH --- %% #
yerrh=(pathLengths['mean'].as_matrix() - pathLengths['1%'].as_matrix()).T
yerrl=(pathLengths['99%'].as_matrix() - pathLengths['mean'].as_matrix()).T
plt.errorbar(x=pathLengths['nodes'], y=pathLengths['mean'], yerr=[yerrh, yerrl],
    fmt='.-.',
    capsize=5,
    elinewidth=1,
    markeredgewidth=1,
    linewidth=1)
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.show()

# %%
sns.distplot(unfold(experiments.iloc[11]['pathLengths']), kde=False)
plt.show()

# %%
pl_dict = experiments.iloc[11]['pathLengths']
pl_pdf = [pl_dict['{}'.format(k)]/float(experiments.iloc[11]['iterations']*(2**12))
        for k in range(14)]
plt.plot(range(14), pl_pdf, lw=1)
plt.show()

# %% --- QUERIES --- %% #
yerrh=(queries['mean'].as_matrix() - queries['1%'].as_matrix()).T
yerrl=(queries['99%'].as_matrix() - queries['mean'].as_matrix()).T
plt.errorbar(x=queries['nodes'], y=queries['mean'], yerr=[yerrh, yerrl],
    fmt='.-.',
    capsize=5,
    elinewidth=1,
    markeredgewidth=1,
    linewidth=1)
plt.xscale('log', basex=2)
# plt.yscale('log', basey=2)
plt.show()

# %%
sns.distplot(unfold(experiments.iloc[11]['queries']), kde=False)
plt.show()

# %%
q_dict = experiments.iloc[11]['queries']
q_keys = sorted([int(k) for k in q_dict.keys()])
q_pdf = [q_dict['{}'.format(k)]/float(experiments.iloc[11]['iterations']*(2**12))
        for k in q_keys]
plt.plot(q_keys, q_pdf, lw=1)
plt.show()
