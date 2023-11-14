From my original design, I didn't need to create new classes, but I did ended up removing quite some others, as I found them redundant, or making them useful would have taken too much time.

I did end up rewritting the pathfindig algorithm that I had in mind, becuase of a mistunderstanding mostly, but the new algorithm is much much better, and probably the old one didn't actually work in all of the cases. And I didn't need to create a completely seperate algorithm to cross the boarder, which is why that particular class didn't make the cut.


The Sequence diagram was rather spot on though.

I am not too statisfied with how dense the classes are, but the methods I think are very well decupled, and the vast majory of the satisfy the single responsibility principle.

I didn't have time to do the CLI that I wanted, with a full rendition of the map, but I settled for showing the moves of the current player only. The CLI is the most basic it could be.

I am satisfied with the logging. I think it covers the basics.
The error handling could be much better. I didn't have time to do much with it.
Unit testing is nonexistent because I did not have time to get into it at all.

-----------------------------------------------------------------------------------------

Map creation: 
- I determine which side border will have a water node and assign it randomly
- I then assign the top border water nodes randomly as well, but I don't put it the first or last node since then it's also a border node, and I check if it *might* an island.
- I check if an island might be created from this idea: To have an island, you need at least two water nodes to be diagonal with eachother, so I don't assign a new node, if it would be diagonal with an already set one. Like so I prohibit the map from creating islands without needing to check afterwards.
- I assign the left water nodes (from 1 to 3 more) randomly around the field, also while checkign the above conditions.
- I then assign the mountain (from 3 to 6) nodes randomly.
- I assign a random grass field with the fort randomly
- I then fill the map with grass fields.
- To make this process much eaiser, I use a one dimensional array, where the index substitues the X and Y coordinates mathematically. Index= X+Y*8


Pathfinder:
- I implemeted a BFS algorithm and used the moves that it would take to arrive to a node as their weight
- To do this, I created another two classes, FullMapWeight and MapWeightNode, to contain the coordinates, the initial weight based on their terrain, and other neccesary information for the algorithm
- It is reusable for the treasure and the fort, and with some minor modificaiton, for crossing the boarder as well
- Through out the algorithm, I use the same idea of the index as with the HalfMap, but modified to accomodate for the bigger map.

Network
- I just used the example client as the base for the rest of the requests. I didn't have time to do anthing fancier.

MVC
- The view is a simple CLI which says the move, when you won, lost, picked up the treausre, crossed the border and found the fort.
- It is also used for other messages such as network errors and such.



