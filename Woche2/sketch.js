// @ts-check

const mutationRate = 0.2;
const populationSize = 50;
const replacementRatio = 0.8;

const maxForce = 2;
const applyFactor = 0.3;
const flightTime = 180;

let rockets = new Array(populationSize);
let obstacles = [];
let goal;
let time = 0;

function setup() {
	createCanvas(800, 800);
	rockets = rockets.fill(null).map(() => new Rocket());
	obstacles.push(new Obstacle(100, 100, 100, 20));
	obstacles.push(new Obstacle(300, 200, 100, 20));
	obstacles.push(new Obstacle(100, 300, 50, 20));
	obstacles.push(new Obstacle(470, 200, 200, 20));
	obstacles.push(new Obstacle(300, 400, 100, 20));
	obstacles.push(new Obstacle(600, 500, 100, 20));
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
		rockets = makeNewGeneration(rockets);
		time = 0;
	} else {
		time++;
	}
}

function makeNewGeneration(rockets) {
	rockets = rockets.sort((r1, r2) => r2.calculateFitness() - r1.calculateFitness());
	console.log(rockets[0].calculateFitness())
	let nextGen = new Array(Math.floor(populationSize * replacementRatio)).fill(null).map(() => pick(rockets));
	nextGen = nextGen.map((rocket, idx, allRockets) => {
		if (idx % 2 === 0) {
			return rocket.crossWith(allRockets[idx + 1]);
		} else {
			return rocket.crossWith(allRockets[idx - 1]);
		}
	})
	return mutate(nextGen).concat(rockets.slice(0, populationSize - nextGen.length).map(r => r.revive()));
}


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

function mutate(nextGen) {
	nextGen.forEach(rocket => {
		let rand = Math.random();
		if (rand < mutationRate) {
			let mutateCount = Math.random() * flightTime / 15;
			for (let idx = 0; idx < mutateCount; idx++) {
				let randIdx = Math.floor(Math.random() * flightTime);
				rocket.vectorArray[randIdx] = new Vector(randomNumber(), randomNumber());
			}
		}
	});
	return nextGen
}

function randomNumber() {
	return Math.random() * maxForce * 2 - maxForce;
}

function mouseDragged() {
	goal.position.x = mouseX;
	goal.position.y = mouseY;
}

class Rocket {
	constructor(vectorArray) {
		if (vectorArray) {
			this.vectorArray = vectorArray;
		} else {
			this.vectorArray = this.generateDirectionsArray(flightTime);
		}

		this.position = {
			x: width / 2,
			y: height * 0.9
		};


		this.size = 5;
		this.bestDistance = 10000;
		this.alive = true;
		this.velocity = new Vector(0, 1);
		this.hitTime = null
	}

	fly(time) {
		const currentVector = this.vectorArray[time];
		this.velocity = this.velocity.addToPosition(currentVector);
		this.position = {
			x: this.position.x + this.velocity.x,
			y: this.position.y - this.velocity.y,
		};
	}

	draw(obstacles, goal) {
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
			this.bestDistance *= 5;
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

	isOutOfScreen() {
		return (
			this.position.x < 0 ||
			this.position.x > width ||
			this.position.y < 0 ||
			this.position.y > height
		);
	}

	calculateFitness() {
		if (this.bestDistance === 0) {
			return 1 - this.hitTime / (flightTime * 2);
		}
		return 1 / this.bestDistance;
	}

	distanceToGoal(goal) {
		let x = this.position.x - goal.position.x;
		let y = this.position.y - goal.position.y;
		return Math.sqrt(x * x + y * y);
	}

	generateDirectionsArray(length) {
		return new Array(length)
			.fill(null)
			.map(() => new Vector(randomNumber(), randomNumber()));
	}

	crossWith(partner) {
		const splitPoint = Math.random() * this.vectorArray.length;
		const myDNA = this.vectorArray.slice(0, splitPoint);
		const partnerDNA = partner.vectorArray.slice(splitPoint);
		return new Rocket(myDNA.concat(partnerDNA));
	}

	revive() {
		return new Rocket(this.vectorArray);
	}
}

class Vector {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}

	addToPosition(position) {
		return new Vector(
			this.x + position.x * applyFactor,
			this.y + position.y * applyFactor
		);
	}
}

class Obstacle {
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