// @ts-check

var startPoint = { x: 0, y: 0 };
var maxForce = 5;
var applyFactor = 0.8;
var fightTime = 255;

var rockets = [];
var obstacles = [];
var goal;

function setup() {
	createCanvas(400, 400);
	rockets.fill(null).map(() => new Rocket());
	obstacles.push(new Obstacle(100, 100, 100, 20));
	obstacles.push(new Obstacle(300, 200, 100, 20));
	goal = new Goal();
	noLoop();
}

function draw() {
	let rocketsAlive = rockets.length;
	for (let time = 0; time < fightTime && rocketsAlive > 0; time++) {
		background(0);
		rockets.forEach(r => r.draw(obstacles, goal));
		rocketsAlive = rockets.filter(r => r.alive).length;
	}
}


class Rocket {

	constructor(vectorArray) {
		if (vectorArray) {
			this.vectorArray = vectorArray;
		} else {
			this.vectorArray = this.generateDirectionsArray(fightTime);
		}

		this.position = startPoint;
		this.vectorArray = [];
		this.size = 50;
		this.bestDistance = 10000;
		this.alive = true;
	}

	fly(time) {
		const gravity = new Vector(0, -1);
		let currentVector = this.vectorArray[time];

		this.position = { x: time, y: time };
	}

	draw(obstacles, goal) {
		if (!this.alive) return;

		this.fly();

		if (this.detectCrashes(obstacles)) {
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

		ellipse(this.position.x, this.position.y, this.size, this.size);
	}

	detectCrashes(obstacles) {
		return obstacles.some(rect => {
			let distX = Math.abs(this.position.x - rect.x - rect.w / 2);
			let distY = Math.abs(this.position.y - rect.y - rect.h / 2);

			if (distX > (rect.w / 2 + this.size)) { return false; }
			if (distY > (rect.h / 2 + this.size)) { return false; }

			if (distX <= (rect.w / 2)) { return true; }
			if (distY <= (rect.h / 2)) { return true; }

			let dx = distX - rect.w / 2;
			let dy = distY - rect.h / 2;
			return (dx * dx + dy * dy <= (this.size * this.size));
		})
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
		return new Array(length).fill(null).map(() => new Vector(Math.random() * maxForce, Math.random() * maxForce));
	}

	fuck(partner) {
		const splitPoint = this.vectorArray.length / 2;
		return new Rocket(this.vectorArray.slice(0, splitPoint).concat(partner.vectorArray.slice(splitPoint)))
	}

}

class Vector {
	constructor(x, y) {
		this.x = x;
		this.y = y;
	}

	addToPosition(position) {
		return { x: position.x + this.x * applyFactor, y: position.y + this.y * applyFactor };
	}
}


class Obstacle {
	constructor(x, y, w, h) {
		this.x = x;
		this.y = y;
		this.sizeX = w;
		this.sizeY = h;
	}
}

class Goal {
	constructor() {
		this.position = { x: 350, y: 200 };
		this.radius = 35;
	}
}