# Algorithm
This software was written for DBL (Design Based Learning) Algorithms by group 11 of 2IO90 year 2017-2018 at the University of Eindhoven. 

## Summary
The software is aimed to solve a particular problem, namely taxi scheduling. Currently in the real world, usually each customer is treated separately. Ride sharing does not happen often. It seems logical that this is not optimal for delivering customers to their destination as fast as possible. This algorithm is designed to schedule taxis as efficient as possible such that the total runtime of the algorithm itself and the costs, as described below, are as low as possible. 

Of course, this algorithm can be extended to be applied in many more fields due to the abstraction of the problem to networks and requests. It must be noted, that the current version of the algorithm is designed to perform optimally on the final test cases provided by the course. However, we think that we've come to a rather good solution. Also, there is plenty of room for further optimalisation. 

## Test case format
Each test case begins with a preamble, next a training period and as last the actual, real, call list.

#### Preamble
The preamble contains information that is requried to run the algorithm. Below is a list of the most important parts
* Structure of the graph (i.e. vertices and edges)
* Number of taxis
* Capacity of each taxi
* Alpha value (explained in Performance)

#### Training period
Next, a training period follows in which the performance of the algorithm is not measured. This can be used to train the algorithm or precalculate certain information. 

#### Call list
Lastly, a list of calls will follow. This is a turned based process. First, a line of the call list is received by the algorithm. This line can contain _n_ number of call, which consists of a start vertex and a destination vertex. Then, the algorithm is allowed to move all taxis, drop or pick up customers. Each taxi is only allowed to move OR to pick up and drop at each turn. When the algorithm is done, the next line of the call list is read. This process continues until each customer arrived at its destination. 

## Performance
The performance is measured by calculating the following cost function:

![Cost function](https://latex.codecogs.com/gif.latex?\large&space;\sum^n_{i&space;=&space;1}\left&space;(&space;\frac{a_i&space;-&space;c_i}{(d(u_i,&space;v_i)&space;&plus;&space;2)^{\alpha}}&space;\right&space;)^2)

* _n_ is the total amount of customers
* _c_ is the call time of a customer _i_
* _a_ is the arrival time of customer _i_ at its destination
* _d(u, v)_ is the shortest path between node _u_ and _v_, where _u_ is the start vertex and _v_ is the destination vertex of the customer

## Intsallation instructions
See [the interpreter](https://github.com/SquarePlayn/2IO90-Interpreter) for installation instructions.
