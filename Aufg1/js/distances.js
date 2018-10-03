// @ts-check
var nrOfTowns = 10;
var coordinateX = 100;
var coordinateY = 100;
var maxIteration = 100;
var threshold = 5;

var printLn = str => console.log(str)

var randint = function (from, to) {
    return Math.floor(Math.random() * (to - from) + from);
}

var getCoordinates = function (nr) {
    var coordinates = [];
    for (var _ = 0; _ < nr; _++) coordinates.push([randint(0, coordinateX), randint(0, coordinateY)]);
    return coordinates
};

var distance = function (x, y) {
    return Math.sqrt(Math.pow(x[0] - y[0], 2) + Math.pow(x[1] - y[1], 2))
};

var getAllDistances = function (coordinates) {
    var distances = [];
    var amount = coordinates.length;
    for (var i = 0; i < amount; i++) {
        var town = [];
        var town1 = coordinates[i];
        for (var j = 0; j < amount; j++) {
            var town2 =
                coordinates[j];
            town.push(distance(town1, town2))
        }
        distances.push(town)
    }
    return distances
};

var tripDistance = function (trip, distances) {
    var sum = 0;
    for (var i = 0; i < trip.length - 1; i++) {
        var currentTown = trip[i];
        var nextTown = trip[i + 1];
        sum += distances[currentTown][nextTown]
    }
    sum += distances[trip[trip.length - 1]][trip[0]];
    return sum
};

var getNewTrip = function (trip) {
    var cloneTrip = trip.splice(0);
    var change1 = randint(1, cloneTrip.length - 1);
    var change2 = randint(1, cloneTrip.length - 1);
    var temp = cloneTrip[change1];
    cloneTrip[change1] = cloneTrip[change2];
    cloneTrip[change2] = temp;
    return cloneTrip
};

var climbOneStep = function (tripList, distances) {
    var currentTripDist = tripDistance(tripList, distances);
    for (var _ = 0; _ < maxIteration; _++) {
        var nextTrip = getNewTrip(tripList);
        var nextTripDist = tripDistance(nextTrip, distances);
        if (currentTripDist > nextTripDist && Math.abs(currentTripDist - nextTripDist) > threshold)
            return nextTrip
    }
    return false
};

var dist = getAllDistances(getCoordinates(nrOfTowns));
let trip = [];
for (var i = 0; i < nrOfTowns; i++) {
    trip.push(i);
}
for (var i = 0; i < nrOfTowns; i++) {
    trip = getNewTrip(trip);
}
printLn(trip);
printLn("starting distance");
printLn(tripDistance(trip, dist));
for (var i = 0; i < maxIteration; i++) {
    var result = climbOneStep(trip, dist);
    if (result) {
        // printLn("improved by: " + (tripDistance(result, dist) - tripDistance(result, dist)));
        trip = result
    } else {
        printLn("step count: " + i);
        break
    }
}
printLn("best trip found:");
printLn(trip);
printLn(tripDistance(trip, dist));

//# sourceMappingURL=distances.map