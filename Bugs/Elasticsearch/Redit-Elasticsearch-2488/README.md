# minimum_master_nodes does not prevent split-brain if splits are intersecting #2488

## Description

link: [https://github.com/elastic/elasticsearch/issues/2488](https://github.com/elastic/elasticsearch/issues/2488)

I'm using ElasticSearch 0.19.11 with the unicast Zen discovery protocol.

With this setup, I can easily split a 3-node cluster into two 'hemispheres' (continuing with the brain metaphor) with one node acting as a participant in both hemispheres. I believe this to be a significant problem, because now minimum_master_nodes is incapable of preventing certain split-brain scenarios.

Here's what my 3-node test cluster looked like before I broke it:



Here's what the cluster looked like after simulating a communications failure between nodes (2) and (3):



Here's what seems to have happened immediately after the split:

Node (2) and (3) lose contact with one another. (zen-disco-node_failed ... reason failed to ping)
Node (2), still master of the left hemisphere, notes the disappearance of node (3) and broadcasts an advisory message to all of its followers. Node (1) takes note of the advisory.
Node (3) has now lost contact with its old master and decides to hold an election. It declares itself winner of the election. On declaring itself, it assumes master role of the right hemisphere, then broadcasts an advisory message to all of its followers. Node (1) takes note of this advisory, too.
At this point, I can't say I know what to expect to find on node (1). If I query both masters for a list of nodes, I see node (1) in both clusters.

Let's look at minimum_master_nodes as it applies to this test cluster. Assume I had set minimum_master_nodes to 2. Had node (3) been completely isolated from nodes (1) and (2), I would not have run into this problem. The left hemisphere would have enough nodes to satisfy the constraint; the right hemisphere would not. This would continue to work for larger clusters (with an appropriately larger value for minimum_master_nodes).

The problem with minimum_master_nodes is that it does not work when the split brains are intersecting, as in my example above. Even on a larger cluster of, say, 7 nodes with minimum_master_nodes set to 4, all that needs to happen is for the 'right' two nodes to lose contact with one another (a master election has to take place) for the cluster to split.

Is there anything that can be done to detect the intersecting split on node (1)?

Would #1057 help?


## Testcase

Elasticsearch version: 1.3.0 (the bug is fixed in v1.4.0.Beta1 according to [official resiliency page](https://www.elastic.co/guide/en/elasticsearch/resiliency/current/index.html))

1. Edit elasticsearch.yml and make the following changes:
    - discovery.zen.minimum_master_nodes: 2
    - discovery.zen.fd.ping_timeout: 7
    - discovery.zen.fd.ping_retries: 2
    
2. Start a 3-node elasticsearch cluster using Redit and find out master server in the cluster using `curl localhost:9200/_cat/master?v`
3. Disconnect network between master and one of the follower.
4. If that follower elects itself as the new master, there will be 2 masters in the cluster at the same time according to description.
5. Run `curl localhost:9200/_cat/master?v` within the master and the follower which thinks itself to be the master, and two different master ip addresses is shown in the console.

This result also reveled by the log files of the 3 nodes, in which 2 nodes declared themselves as a master.

Noted that due to the election mechanism of elasticsearch, not every single test can trigger this bug, so we wrote a loop that keeps repeating the single test process above.
The test loop will be stopped if the bug is triggered or the maximum number of attempts is achieved.

We ran 25 rounds of test and the bug was triggered 12 times.

