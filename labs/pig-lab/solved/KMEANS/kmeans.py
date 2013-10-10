#!/usr/bin/python
import sys
from math import fabs
from math import sqrt
from org.apache.pig.scripting import Pig

filename = "1.log"
k = 5
tolerance = 0.01

MAX_SCORE = 180
MIN_SCORE = -180
MAX_ITERATION = 100

# initial centroid, equally divide the space
# initial_centroids = ""
# last_centroids = [(None,None)] * k
# for i in range(k):
#     last_centroids[i] = (MIN_SCORE + float(i)/k*(MAX_SCORE-MIN_SCORE),MIN_SCORE + float(i)/k*(MAX_SCORE-MIN_SCORE))
#     initial_centroids = initial_centroids + str(last_centroids[i])
#     if i!=k-1:
#         initial_centroids = initial_centroids + ":"

initial_centroids = "-120.0,-120.0:-60.0,-60.0:0.0, 0.0:60.0,60.0:120.0,120.0"
last_centroids = [(-120.0,-120.0),(-60.0, -60.0),(0.0, 0.0),(60.0, 60.0),(120.0,120.0)]

print initial_centroids



P = Pig.compile("""register /Users/yun_shen/Desktop/spams/pigudf.jar
                   DEFINE find_centroid FindCentroid('$centroids');
                   raw_data = load '1.log' as (spam_id:chararray, longitude:double, latitude:double);
                   raw = filter raw_data by longitude is not null and latitude is not null;
                   centroided = foreach raw generate spam_id, longitude, latitude, find_centroid(longitude, latitude) as centroid;
                   grouped = group centroided by centroid parallel 4;
                   store grouped into 'grouped';
                   result = foreach grouped generate group, AVG(centroided.longitude), AVG(centroided.latitude);
                   store result into 'output';
                """)

converged = False
iter_num = 0
while iter_num < MAX_ITERATION:
	Q = P.bind({'centroids':initial_centroids})
	results = Q.runSingle()
	if results.isSuccessful() == "FAILED":
		raise "Pig job failed"
	iter = results.result("result").iterator()
	centroids = []
	x = 0.0
	y = 0.0
	distance_move = 0
	# get new centroid of this iteration, caculate the moving distance with last iteration
	initial_centroids = ""
	for i in range(k):
		tuple = iter.next()
		x = float(str(tuple.get(1)))
		y = float(str(tuple.get(2)))
		x_move = (last_centroids[i][0] - x)**2
		y_move = (last_centroids[i][1] - y)**2
		distance_move = distance_move + sqrt(x_move + y_move)
		print distance_move
	
		new_centroid = (x,y)
		centroids.append(new_centroid)
	
		initial_centroids = initial_centroids + str(x) + "," + str(y)
	
		if i!=k-1:
			initial_centroids = initial_centroids + ":"

	iter_num = iter_num + 1

	distance_move = distance_move / k;
	if distance_move>tolerance:
	    Pig.fs("rmr grouped")
	Pig.fs("rmr output")
	print("iteration " + str(iter_num))
	print("average distance moved: " + str(distance_move))
	if distance_move<tolerance:
		sys.stdout.write("k-means converged at centroids: [")
		sys.stdout.write(",".join(str(v) for v in centroids))
		sys.stdout.write("]\n")
		converged = True
		break


	last_centroids = centroids

	print last_centroids
	print initial_centroids

