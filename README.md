# PubMedComments

### Quick Start
Download `PubMedComments.jar` from the repository's root directory.

The jar should be run as follows:
```
java -jar PubMedComments.jar PMID API_KEY REQUESTS_PER_SECOND
```
Note that the order of these parameters does matter.

### Description
Creates a tree with the provided PMID's title and comments.
Next, it recursively iterates through each comment, adding each PMID and title to the tree.
Once the tree is fully explored, it maps the tree by the depth and returns the furthest title.

```
Depth: PMID
2:  27405686
        |
1:  28975612
        |
0:  28975607
```

In this example, the PMID `27405686`, at depth `2`, is the furthest  title.
When the tree is converted to a map, the keys are integers and the values are sets of titles.
If the PMID at depth `1` split off into two comments, in the map, the set at depth `2` would be the two PMIDs that `1` split into. 
Finally, the set at depth `0`, which is the query, is removed because it cannot be a result.
