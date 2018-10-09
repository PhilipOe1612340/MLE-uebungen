// @ts-check

const mutationRate = 0.1;
const populationSize = 100;

const startPoint = {
	x: 400,
	y: 700
};
const maxForce = 5;
const applyFactor = 0.3;
const flightTime = 200;

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
	// frameRate(1)
}

function draw() {
	background(0);
	goal.draw();
	obstacles.forEach(o => o.draw());
	rockets.forEach(r => r.draw(obstacles, goal));
	let rocketsAlive = rockets.filter(r => r.alive).length;
	if (time >= flightTime - 1 || rocketsAlive === 0) {
		rockets = orgie(rockets);
		time = 0;
	} else {
		time++;
	}
}

function orgie(rockets) {
	let nextGen = [];
	let sorted = rockets.sort((r1, r2) => r2.calculateFitness() - r1.calculateFitness())
	sorted.forEach((r, i, rockets) => {
		if (getsPicked(i)) {
			const partner = rockets.find((_rocket, i, r) => getsPicked(i))
			nextGen.push(r.fuck(partner))
			nextGen.push(partner.fuck(r))
		}
	});
	console.log(nextGen.length);
	console.log(sorted[0].calculateFitness())

	if (nextGen.length > populationSize) {
		nextGen = nextGen.slice(0, populationSize);
	}
	nextGen = nextGen.concat(sorted.slice(0, populationSize - nextGen.length).map(r => new Rocket(r.vectorArray)))
	return mutate(nextGen);
}

function mutate(nextGen) {
	return nextGen.map(rocket => {
		rocket.vectorArray.map(vector => {
			if (Math.random() < mutationRate) {
				vector.x = randomNumber();
				vector.y = randomNumber();
			}
			return vector;
		})
		return rocket;
	})
}

function getsPicked(idx) {
	const probability = 3 / idx
	const rand = Math.random();
	return rand < probability;
}

function randomNumber() {
	return Math.random() * maxForce - maxForce / 2
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

		this.position = startPoint;
		this.size = 5;
		this.bestDistance = 10000;
		this.alive = true;
		this.velocity = new Vector(0, 1);
	}

	fly(time) {
		const gravity = 0;
		const currentVector = this.vectorArray[time];
		this.velocity = this.velocity.addToPosition(currentVector);
		this.velocity.y += gravity;
		this.position = {
			x: this.position.x + this.velocity.x,
			y: this.position.y - this.velocity.y
		};
	}

	draw(obstacles, goal) {
		if (!this.alive) return;

		this.fly(time);

		if (this.detectCrashes(obstacles)) {
			console.log('killed');
			this.bestDistance *= 5;
			this.alive = false;
		}
		let currentDistance = this.distanceToGoal(goal);

		if (currentDistance < this.size + goal.radius) {
			currentDistance = 0;
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
		return this.isOutOfScreen() || obstacles.some(rect => {
			let distX = Math.abs(this.position.x - rect.x - rect.w / 2);
			let distY = Math.abs(this.position.y - rect.y - rect.h / 2);

			if (distX > (rect.w / 2 + this.size)) {
				return false;
			}
			if (distY > (rect.h / 2 + this.size)) {
				return false;
			}

			if (distX <= (rect.w / 2)) {
				return true;
			}
			if (distY <= (rect.h / 2)) {
				return true;
			}

			let dx = distX - rect.w / 2;
			let dy = distY - rect.h / 2;
			return (dx * dx + dy * dy <= (this.size * this.size));
		})
	}

	isOutOfScreen() {
		return this.position.x < 0 || this.position.x > 800 || this.position.y < 0 || this.position.y > 800;
	}

	calculateFitness() {
		return this.bestDistance * -1;
	}

	distanceToGoal(goal) {
		let x = this.position.x - goal.position.x;
		let y = this.position.y - goal.position.y;
		return Math.sqrt((x * x) + (y * y));
	}

	generateDirectionsArray(length) {
		return new Array(length).fill(null).map(() => new Vector(randomNumber(), randomNumber()));
	}

	fuck(partner) {
		const splitPoint = Math.random() * this.vectorArray.length;
		const myDNA = this.vectorArray.slice(0, splitPoint);
		const partnerDNA = partner.vectorArray.slice(splitPoint);
		return new Rocket(myDNA.concat(partnerDNA));
	}

}

class Vector {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}

	addToPosition(position) {
		return new Vector(this.x + position.x * applyFactor, this.y + position.y * applyFactor);
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
		fill(255, 0, 0);
		ellipse(this.position.x, this.position.y, this.radius, this.radius);
	}
}