import math
from random import randint
import matplotlip.pyplot as plt

nrOfTowns = 100
coordinateX = 1000
coordinateY = 1000
maxIteration = 1000
threshold = 5

# Returns random coordinates for a number of towns
def getCoordinates(nr):
    coordinates = []
    for _ in range(nr):
        coordinates.append((randint(0, coordinateX), randint(0, coordinateY)))
    return coordinates


# calculates a distance between two towns
def distance(x, y):
    return math.sqrt((x[0]-y[0]) ** 2 + (x[1]-y[1]) ** 2)


# calculates the distance beween all towns
def getAllDistances(coordinates):
    distances = []
    amount = len(coordinates)
    for i in range(amount):
        town = []
        town1 = coordinates[i]
        for j in range(amount):
            town2 = coordinates[j]
            town.append(distance(town1, town2))
        distances.append(town)
    return distances


# pretty prints a matrix (hopefuly)
def printMatrix(matrix):
    print('\n'.join(['\t'.join([str(math.ceil(cell))
                                for cell in row]) for row in matrix]))


# calculate the length of a round trip
def tripDistance(trip, distances):
    sum = 0
    for i in range(len(trip) - 1):
        currentTown = trip[i]
        nextTown = trip[i+1]
        sum += distances[currentTown][nextTown]
    sum += distances[len(trip) - 1][0]
    return sum


# switch two random towns
def getNewTrip(trip):
    # starting position of roundtrip remains the same
    cloneTrip = trip[:]
    change1 = randint(1, len(cloneTrip) - 1)
    change2 = randint(1, len(cloneTrip) - 1)
    temp = cloneTrip[change1]
    cloneTrip[change1] = cloneTrip[change2]
    cloneTrip[change2] = temp
    return cloneTrip


# searches for the first better trip it finds. If there is none it returns false.
def climbOneStep(trip, distances):
    for _ in range(maxIteration):
        currentTripDist = tripDistance(trip, distances)
        nextTrip = getNewTrip(trip)
        nextTripDist = tripDistance(nextTrip, distances)
        if currentTripDist > nextTripDist and abs(currentTripDist - nextTripDist) > threshold:
            return nextTrip
    return False


# generate distance matrix
dist = getAllDistances(getCoordinates(nrOfTowns))
# printMatrix(dist)

# put all towns in to the round trip
trip = []
for i in range(nrOfTowns):
    trip.append(i)

# randomize trip
for _ in range(nrOfTowns):
    trip = getNewTrip(trip)

print(trip)
print("starting distance")
print(tripDistance(trip, dist))


for i in range(maxIteration):
    result = climbOneStep(trip, dist)
    if result:
        # print("improved by: " + str(tripDistance(trip, dist) - tripDistance(result, dist)))                                                    
        trip = result
        plt.plot(i, result)
    else:
        print("step count:" + str(i))
        break
print("best trip found:")
print(trip)
print(tripDistance(trip, dist))
plt.show()


