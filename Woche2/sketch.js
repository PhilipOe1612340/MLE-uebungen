// @ts-check

const mutationRate = 0.17;
const populationSize = 100;
const replacementRatio = 0.8;

const maxForce = 1.8;
const applyFactor = 0.3;
const flightTime = 150;
const nrOfObstacles = 10;

/** @type {Rocket[]}  */
let rockets = new Array(populationSize);
let obstacles = [];
let goal;
let time = 0;
let generationCounter = 0;
let bestHistory = [];
let reachingGoal = 0;

function setup() {
    createCanvas(800, 800);

    // generate Rockets
    rockets = rockets.fill(null).map(() => new Rocket());

    // set obstacles
    obstacles = makeObstacles();

    // create goal
    goal = new Goal();
}

function draw() {
    background(0);

    //draw objects
    goal.draw();
    obstacles.forEach(o => o.draw());

    // draw object
    rockets.forEach(r => r.draw(obstacles, goal));
    let rocketsAlive = rockets.filter(r => r.alive).length;

    //generate new population if time is over or all rockets are crashed
    if (time >= flightTime - 1 || rocketsAlive === 0) {
        // count successful rockets
        reachingGoal = rockets.filter(r => r.hitTime).length;

        // next Generation
        rockets = makeNewGeneration(rockets);
        generationCounter++;
        time = 0;
    } else {
        time++;
    }

    drawHistory(bestHistory);

    // draw text
    fill(0, 102, 153);
    stroke(0);
    textSize(20);
    text(`Generation: ${generationCounter}`, 30, height - 90);
    text(`Erfolgreich: ${reachingGoal}/${populationSize}`, 30, height - 60);
    textSize(16);
    text(`Time: ${time}`, 30, height - 30);
}


/**
 * @param {Rocket[]} rockets 
 * @returns Rocket[]
 */
function makeNewGeneration(rockets) {
    // sort rockets by fitness
    rockets = rockets.sort((r1, r2) => r2.calculateFitness() - r1.calculateFitness());

    bestHistory = rockets[0].history;

    // pick rockets in mating pool
    let nextGen = new Array(Math.floor(populationSize * replacementRatio))
        .fill(null)
        .map(() => pick(rockets));

    // cross rockets in mating pool
    nextGen = nextGen.map((rocket, idx, allRockets) => {
        if (idx % 2 === 0) {
            return rocket.crossWith(allRockets[idx + 1]);
        } else {
            return rocket.crossWith(allRockets[idx - 1]);
        }
    });

    // mutate next generation and add top 20% of last gen
    return mutate(nextGen).concat(
        rockets.slice(0, populationSize - nextGen.length).map(r => r.revive())
    );
}

/**
 * @param {Rocket[]} rockets 
 * @returns Rocket[] 
 */
function pick(rockets) {
    let fitnessSum = rockets.reduce((sum, current) => sum + current.calculateFitness(), 0);
    let rand = Math.random();
    let sum = 0;

    for (let idx = 0; idx < rockets.length; idx++) {
        const rocket = rockets[idx];
        sum += rocket.calculateFitness() / fitnessSum;
        if (sum > rand) {
            return rocket;
        }
    }
}

/**
 * @param {Rocket[]} nextGen 
 * @returns Rocket[] 
 */
function mutate(nextGen) {
    nextGen.forEach(rocket => {
        let rand = Math.random();
        if (rand < mutationRate) {
            let mutateCount = (Math.random() * flightTime) / 15;
            for (let idx = 0; idx < mutateCount; idx++) {
                let randIdx = Math.floor(Math.random() * flightTime);
                rocket.vectorArray[randIdx] = new Vector(
                    randomNumber(),
                    randomNumber()
                );
            }
        }
    });
    return nextGen;
}

/**
 * @returns number
 */
function randomNumber() {
    return Math.random() * maxForce * 2 - maxForce;
}

function mouseDragged() {
    goal.position.x = mouseX;
    goal.position.y = mouseY;
}


function makeObstacles() {
    const obst = [];
    for (let i = 0; i < nrOfObstacles; i++) {
        obst.push(new Obstacle(Math.random() * width, Math.random() * height, Math.random() * 100 + 50, Math.random() * 20 + 20));
    }
    return obst;
}

function keyPressed() {
    if (keyCode === LEFT_ARROW) {
        obstacles = makeObstacles();
    }
}

function drawHistory(history) {
    history.forEach((element, i, elements) => {
        const next = elements[i + 1]
        if (next) {
            stroke(255, 0, 0);
            line(element.x, element.y, next.x, next.y);
        }
    });
}

class Rocket {

    /**
     * @param {Vector[] | undefined} vectorArray 
     */
    constructor(vectorArray) {
        if (vectorArray) {
            this.vectorArray = vectorArray;
        } else {
            this.vectorArray = this.generateDirectionsArray(flightTime);
        }

        // init values
        this.position = { x: width / 2, y: height * 0.9 };
        this.size = 5;
        this.bestDistance = 10000;
        this.alive = true;
        this.velocity = new Vector(0, 0);
        this.hitTime = null;
        this.history = [];
    }

    /**
     * @param {number} time 
     */
    fly(time) {
        const currentVector = this.vectorArray[time];
        this.velocity = this.velocity.addToPosition(currentVector);
        this.position = {
            x: this.position.x + this.velocity.x,
            y: this.position.y - this.velocity.y,
        };
    }

    /**
     * @param {Obstacle[]} obstacles 
     * @param {Goal} goal 
     */
    draw(obstacles, goal) {
        this.history.push(this.position);
        if (!this.alive) {
            stroke(255, 0, 0);
            ellipse(
                this.position.x,
                this.position.y,
                this.size / 2,
                this.size / 2
            );
            return;
        }

        this.fly(time);

        if (this.detectCrashes(obstacles)) {
            this.bestDistance *= 500;
            this.alive = false;
        }
        let currentDistance = this.distanceToGoal(goal);

        if (currentDistance < 20) {
            currentDistance = 0;
            this.hitTime = time;
            this.alive = false;
        }
        if (currentDistance < this.bestDistance) {
            this.bestDistance = currentDistance;
        }
        stroke(255);
        fill(180);
        ellipse(this.position.x, this.position.y, this.size, this.size);
    }

    /**
     * @param {Obstacle[]} obstacles 
     * @returns boolean
     */
    detectCrashes(obstacles) {
        return (
            this.isOutOfScreen() ||
            obstacles.some(rect => {
                let distX = Math.abs(this.position.x - rect.x - rect.w / 2);
                let distY = Math.abs(this.position.y - rect.y - rect.h / 2);

                if (distX > rect.w / 2 + this.size) {
                    return false;
                }
                if (distY > rect.h / 2 + this.size) {
                    return false;
                }

                if (distX <= rect.w / 2) {
                    return true;
                }
                if (distY <= rect.h / 2) {
                    return true;
                }

                let dx = distX - rect.w / 2;
                let dy = distY - rect.h / 2;
                return dx * dx + dy * dy <= this.size * this.size;
            })
        );
    }


    /**
     * @returns boolean
     */
    isOutOfScreen() {
        return (
            this.position.x < 0 ||
            this.position.x > width ||
            this.position.y < 0 ||
            this.position.y > height
        );
    }

    /**
     * @returns number
     */
    calculateFitness() {
        if (this.bestDistance === 0) {
            return 1 - this.hitTime / (flightTime * 2);
        }
        return 1 / this.bestDistance;
    }

    /**
     * @param {Goal} goal 
     * @returns number
     */
    distanceToGoal(goal) {
        let x = this.position.x - goal.position.x;
        let y = this.position.y - goal.position.y;
        return Math.sqrt(x * x + y * y);
    }

    /**
     * @param {number} length 
     * @returns Vector[]
     */
    generateDirectionsArray(length) {
        return new Array(length)
            .fill(null)
            .map(() => new Vector(randomNumber(), randomNumber()));
    }

    /**
     * @param {Rocket} partner 
     * @returns Rocket
     */
    crossWith(partner) {
        const splitPoint = Math.random() * this.vectorArray.length;
        const myDNA = this.vectorArray.slice(0, splitPoint);
        const partnerDNA = partner.vectorArray.slice(splitPoint);
        return new Rocket(myDNA.concat(partnerDNA));
    }

    /**
     * @returns Rocket
     */
    revive() {
        return new Rocket(this.vectorArray);
    }
}

class Vector {

    /**
     * @param {number} x 
     * @param {number} y 
     */
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param {Vector} position 
     */
    addToPosition(position) {
        return new Vector(
            this.x + position.x * applyFactor,
            this.y + position.y * applyFactor
        );
    }
}

class Obstacle {
    /**
     * @param {number} x 
     * @param {number} y 
     * @param {number} w 
     * @param {number} h 
     */
    constructor(x, y, w, h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    draw() {
        fill(0, 0, 40);
        stroke(255);
        rect(this.x, this.y, this.w, this.h);
    }
}

class Goal {
    constructor() {
        this.position = {
            x: 350,
            y: 100
        };
        this.radius = 35;
    }

    draw() {
        stroke(255);
        fill(255, 0, 0);
        ellipse(this.position.x, this.position.y, this.radius, this.radius);
    }
}
