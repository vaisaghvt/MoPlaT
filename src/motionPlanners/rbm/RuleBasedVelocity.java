///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package motionPlanners.rbm;
//
//import java.awt.Color;
//import java.util.Random;
//import java.util.Vector;
//import javax.vecmath.Vector2d;
//import motionPlanners.rvo.RVOBase;
///**
// * RuleBasedVelocity
// *
// * @author michaellees
// * Created: Nov 30, 2010
// *
// * Copyright michaellees
// *
// * Description:
// *
// * This is the rule based motion planning system as implemented in The Visual Computer paper.
// * The original code was developed by Muzhou Xiong and later adapted by Michael Lees.
// *
// * TODO: Fix all this!
// *
// */
//public class RuleBasedVelocity extends RVOBase{
//
//
//    public int type = 0;
//
//     private static long index = 0;
//    private long agentID;
//
//    private Vector2d oldSelectedVelocity;
//
//
//    private boolean canMoving = true;
//
//    //private Point3D maxTTCV = new Point3D();
//
//
//    private double rho = 0;
//    private double currentTTCThreshold = 0;
//    private double capSpeed = 0;
//
//    private int seed = 10;
//    private Random randGen = new Random(seed);
//
//    private double density = 0;
//    // Maximum density of agents per square metre
//    private double maxDensity = 6.5;
//
//    private Vector2d preferredDirection;
//
//    private double maxTimeToCollision = Double.NEGATIVE_INFINITY;
//
//    public double simulationCost = 0;
//
//    public int collisiontNumber = 0;
//
//
//    /**
//     *
//     */
//
//
//    private double stopTime = 0;
//
//    /*
//     * the following to needs to be initialized in the initializor
//     *  0: normal- walk 1: hurry - rapid walk 2: emergency - run
//     */
//    private int panicLevel = 0;
//    private int mass = 1;
//    // private double _radius = 0.15;
//    private double _radius = 0.2;
//
//    public RuleBasedVelocity(int panic,
//	    int m, double r) {
//	this.setAgentID(index);
//	index++;
//	this.panicLevel = panic;
//	this.mass = m;
//	this._radius = r;
//    }
//
//    /**
//     * get the velocity based on VO or RVO
//     *
//     * @param agentList
//     * @return
//     */
//    public int think(Bag agentList) {
//	this.preferredDirection =
//	this.oldSelectedVelocity.x = this.selectedVelocity.x;
//	this.oldSelectedVelocity.y = this.selectedVelocity.y;
//	// maxTimeToCollision = Double.NEGATIVE_INFINITY;
//	this.collisiontNumber = 0;
//	this.simulationCost = 0;
//	collision = false;
//
//
//	calculateParameters(agentList);
//
//	maxTimeToCollision = Double.NEGATIVE_INFINITY;
//
//	selectNeighbor(agentList);
//
//
//	if (this.canMoving == true && collision == true
//		&& this.neighbors.firstElement() != null) {
//	    this.selectedVelocity = this.collisionResponse(this.neighbors
//		    .firstElement());
//	    this.selectedVelocity = selectVelocityFromSectorSeperation(this,
//		    this.preferredDirection, this.neighbors, 0,
//		    this.capSpeed, 0, 180);
//	    return 0;
//	} else if (this.canMoving == false && collision == true) {
//	    this.selectedVelocity = selectVelocityFromSectorSeperation(this,
//		    this.preferredDirection, this.neighbors, 0,
//		    // 2.5
//		    this.capSpeed, 0, 180);
//
//	    return 0;
//	} else if (this.canMoving == false && collision == false) {
//	    this.selectedVelocity = new Point3D();
//	    return 0;
//	}
//
//	if (collision == false && this.canMoving == true) {
//	    // && !arrived
//	    // 1. observe the neighbor nearby and getting the velocity and
//	    // position
//	    // of them
//
//	    // 2. select a correct Velocity
//	    // selectVelocity1(false);
//	    if (neighbors.size() == 0) {
//		this.selectedVelocity = this.preferredDirection.scale(
//			this.preferredSpeed);
//	    } else {
//		selectNewVelocity();
//	    }
//
//	}
//
//
//	return 0;
//
//    }
//
//
//
//    /**
//     * This method is newly designed. Rather than RVO method using the penalty
//     * formula, it uses a rule-based method to select/evaluate velocities
//     */
//    private int selectNewVelocity() {
//
//	switch (this.panicLevel) {
//	case 0:
//	    // if (this.agentID == 0) {
//	    // System.out.println("");
//	    // }
//	    if (selectVelocityFromSector(0.7 * this.preferredSpeed,
//		    this.preferredSpeed, 0, 60) == true) {
//		// if(this.agentID == 0){
//		// System.out.println("agentID:" + this.agentID
//		// + " threshold:" + this.currentTTCThreshold
//		// + " max ttc: " + this.maxTimeToCollision);
//		// }
//
//		return 0;
//	    }
//	    if (selectVelocityFromSector(this.preferredSpeed,
//		    1.3 * this.preferredSpeed, 0, 30) == true) {
//
//		// if(this.agentID == 0){
//		// System.out.println("agentID:" + this.agentID
//		// + " threshold:" + this.currentTTCThreshold
//		// + " max ttc: " + this.maxTimeToCollision);
//		// }
//
//		return 0;
//	    }
//	    if (selectVelocityFromSector(0.3 * this.preferredSpeed,
//		    0.7 * this.preferredSpeed, 0, 70) == true) {
//
//		// if(this.agentID ==0){
//		// System.out.println("agentID:" + this.agentID
//		// + " threshold:" + this.currentTTCThreshold
//		// + " max ttc: " + this.maxTimeToCollision);
//		// }
//
//		return 0;
//	    }
//	    if (selectVelocityFromSector(0 * this.preferredSpeed,
//		    0.3 * this.preferredSpeed, 0, 90) == true) {
//
//		// if(this.agentID == 0){
//		// System.out.println("agentID:" + this.agentID
//		// + " threshold:" + this.currentTTCThreshold
//		// + " max ttc: " + this.maxTimeToCollision);
//		// }
//
//		return 0;
//	    }
//
//	    break;
//	case 1:
//	    if (selectVelocityFromSector(0.7 * this.preferredSpeed,
//		    this.preferredSpeed, 0, 45) == true) {
//		return 0;
//	    }
//	    if (selectVelocityFromSector(this.preferredSpeed,
//		    1.1 * this.preferredSpeed, 0, 45 / 2) == true) {
//		return 0;
//	    }
//	    if (selectVelocityFromSector(0.4 * this.preferredSpeed,
//		    0.7 * this.preferredSpeed, 0, 60) == true) {
//		return 0;
//	    }
//
//	    break;
//	case 2:
//	    if (selectVelocityFromSector(0.8 * this.preferredSpeed,
//		    this.preferredSpeed, 0, 15) == true) {
//		return 0;
//	    }
//	    if (selectVelocityFromSector(0.5 * this.preferredSpeed,
//		    0.8 * this.preferredSpeed, 0, 30) == true) {
//		return 0;
//	    }
//	}
//	double timeOfCurrentVel = evaluateOldSelectedVelocity();
//	if (this.panicLevel == 0 && this.maxTimeToCollision <= timeOfCurrentVel
//		&& this.oldSelectedVelocity.length() > 0) {
//	    this.selectedVelocity.x = this.oldSelectedVelocity.x;
//	    this.selectedVelocity.y = this.oldSelectedVelocity.y;
//
//	}
//	return 0;
//    }
//
//    /**
//     * select the best velocity from the given region, which is a sector with
//     * the following four parameters
//     *
//     * @param lowerBoundSpeed
//     *            the minimum radius of the sector
//     * @param upperBoundSpeed
//     *            the maximum radius of the sector
//     * @param lowerBoundAngle
//     *            the minimum angle deviation from the centre line(preferred
//     *            direction) of the sector
//     * @param upperBoundAngle
//     *            the maximum angle deviation from the centre line of the sector
//     * @return
//     */
//    private boolean selectVelocityFromSector(double lowerBoundSpeed,
//	    double upperBoundSpeed, double lowerBoundAngle,
//	    double upperBoundAngle) {
//	// The maximum time to collision in this sector. Initialized with -1
//	// double maxTTC = -1;
//	// Point3D preferredDirection = this.getGoal().substract(
//	// this.getCurrentLocation()).normalize();
//	if (lowerBoundSpeed > upperBoundSpeed
//		|| lowerBoundAngle > upperBoundAngle) {
//	    try {
//		throw new Exception("Illegal Parameters");
//	    } catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.toString();
//	    }
//	}
//
//	// int numberOfSpeed =
//	// (int)((upperBoundSpeed-lowerBoundSpeed)/speedInterval);
//	// int numberOfAngle =
//	// (int)((upperBoundAngle-lowerBoundAngle)/angleInterval);
//
//	for (double samplingSpeed = upperBoundSpeed; samplingSpeed >= lowerBoundSpeed; samplingSpeed -= AgentSharedData.speedInterval) {
//	    // double samplingSpeed = upperBoundSpeed
//	    // - speedInterval;
//	    for (double samplingAngle = lowerBoundAngle; samplingAngle <= upperBoundAngle; samplingAngle += AgentSharedData.angleInterval) {
//		// double samplingAngle = lowerBoundAngle + angleInterval;
//		Point3D candidateV1 = preferredDirection
//			.multiply(samplingSpeed).antiClockWiseRotateVector(
//				samplingAngle);
//		Point3D candidateV2 = preferredDirection
//			.multiply(samplingSpeed).antiClockWiseRotateVector(
//				(-1) * samplingAngle);
//
//		double time1 = Double.POSITIVE_INFINITY;
//		double time2 = Double.POSITIVE_INFINITY;
//		for (AgentNewDesign neighborAround : neighbors) {
//		    Point3D vab1 = new Point3D();
//		    Point3D vab2 = new Point3D();
//		    // vab1 = candidateV1.multiply(2).substract(
//		    // neighborAround.getCurrentVelocity()).substract(
//		    // this.currentVelocity);
//		    // vab2 = candidateV2.multiply(2).substract(
//		    // neighborAround.getCurrentVelocity()).substract(
//		    // this.currentVelocity);
//		    // if (this.isInVisionField(this, neighborAround) == true
//		    // && isInVisionField(neighborAround, this) == true
//		    // && neighborAround.canMoving == true
//		    // // && this.panicLevel != 2
//		    // // && neighborAround.panicLevel != 2
//		    // // && neighborAround.arrived == false
//		    // ) {
//		    // // vab1 = candidateV1.multiply(2).substract(
//		    // // neighborAround.getCurrentVelocity()).substract(
//		    // // this.currentVelocity);
//		    // // vab2 = candidateV2.multiply(2).substract(
//		    // // neighborAround.getCurrentVelocity()).substract(
//		    // // this.currentVelocity);
//		    // vab1 = candidateV1.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // vab2 = candidateV2.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // } else if (((isInVisionField(this, neighborAround) ==
//		    // true && (isInVisionField(
//		    // neighborAround, this) == false))
//		    // || neighborAround.canMoving == false
//		    // // || this.panicLevel == 2 || neighborAround.panicLevel
//		    // == 2
//		    // || neighborAround.arrived == true)) {
//		    // vab1 = candidateV1.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // vab2 = candidateV2.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // }
//		    // // else if (neighborAround.arrived==true){
//		    // // vab1 = candidateV1.multiply(2).substract(
//		    // // neighborAround.getCurrentVelocity()).substract(
//		    // // this.currentVelocity);
//		    // // vab2 = candidateV2.multiply(2).substract(
//		    // // neighborAround.getCurrentVelocity()).substract(
//		    // // this.currentVelocity);
//		    // // }
//		    // else {
//		    // vab1 = candidateV1.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // vab2 = candidateV2.substract(neighborAround
//		    // .getCurrentVelocity());
//		    // // continue;
//		    // }
//
//		    vab1 = candidateV1.substract(neighborAround
//			    .getCurrentVelocity());
//		    vab2 = candidateV2.substract(neighborAround
//			    .getCurrentVelocity());
////		   vab1 = candidateV1.multiply(2).substract(
////		      neighborAround.getCurrentVelocity()).substract(
////		      this.currentVelocity);
////		      vab2 = candidateV2.multiply(2).substract(
////		      neighborAround.getCurrentVelocity()).substract(
////		      this.currentVelocity);
//
//		    // Point3D vab = vCand.multiply(1).substract(
//		    // neighborAround.getCurrentVelocity());
//		    double temp_time1 = timeTocollision(this
//			    .getCurrentLocation(), vab1, neighborAround
//			    .getCurrentLocation(),
//			    (this._radius + neighborAround._radius), _collision);
//		    double temp_time2 = timeTocollision(this
//			    .getCurrentLocation(), vab2, neighborAround
//			    .getCurrentLocation(),
//			    (this._radius + neighborAround._radius), _collision);
//		    if (temp_time1 < time1) {
//			time1 = temp_time1;
//		    }
//		    if (temp_time2 < time2) {
//			time2 = temp_time2;
//		    }
//		}// End for (AgentNewDesign neighborAround
//
//		for (RectangleObstacle o : this.obsctalces) {
//		    double temp_time1 = timeTocollision(this
//			    .getCurrentLocation(), candidateV1, o.p1, o.p2,
//			    _collision);
//		    double temp_time2 = timeTocollision(this
//			    .getCurrentLocation(), candidateV2, o.p1, o.p2,
//			    _collision);
//		    if (temp_time1 < time1) {
//			time1 = temp_time1;
//		    }
//		    if (temp_time2 < time2) {
//			time2 = temp_time2;
//		    }
//		}
//
//		double temp = Math.max(time1, time2);
//		Point3D currentV = time1 > time2 ? candidateV1 : candidateV2;
//		if (temp > this.currentTTCThreshold) {
//		    this.selectedVelocity.x = currentV.x;
//		    this.selectedVelocity.y = currentV.y;
//		    this.selectedVelocity.z = currentV.z;
//		    this.maxTimeToCollision = temp;
//		    return true;
//		}
//		if (this.maxTimeToCollision < temp) {
//		    this.maxTimeToCollision = temp;
//		    this.selectedVelocity.x = currentV.x;
//		    this.selectedVelocity.y = currentV.y;
//		    this.selectedVelocity.z = currentV.z;
//		}
//
//	    }// End for (int angleIndex
//	}// End for (int speedIndex
//
//	return false;
//    }
//
//    private double evaluateOldSelectedVelocity() {
//	double minTimeToCollision = Double.POSITIVE_INFINITY;
//	for (AgentNewDesign neighborAround : neighbors) {
//	    if (this.getAgentID() != neighborAround.getAgentID()) {
//		Point3D vab = this.oldSelectedVelocity.substract(neighborAround
//			.getCurrentVelocity());
//		double time = timeTocollision(this.getCurrentLocation(), vab,
//			neighborAround.getCurrentLocation(),
//			(this._radius + neighborAround._radius), _collision);
//		if (time < minTimeToCollision) {
//		    minTimeToCollision = time;
//		}
//	    }
//	}
//	return minTimeToCollision;
//    }
//
//
//
///*
//    private boolean isInVisionField(AgentNewDesign main, AgentNewDesign other) {
//	Vector2d position_Other = other.getCurrentLocation();
//	Vector2d position_Main = main.getCurrentLocation();
//	Vector2d velocity_Main = main.getCurrentVelocity();
//	Vector2d tempV = position_Other.substract(position_Main);
//	if (tempV.length() >= AgentSharedData.VISION_FIELD_LENGTH) {
//	    return false;
//	}
//
//	if (tempV.normalize().dotProduct(velocity_Main.normalize()) >= Math
//		.cos(Math.toRadians(AgentSharedData.VISION_FIELD_ANGLE / 2))) {
//	    return true;
//	} else {
//	    return false;
//	}
//    }
//*/
//
//    private void calculateParameters(Vector<AgentNewDesign> agentList) {
//
//	int number = 1;
//	double maxDistance = 0;
//	for (AgentNewDesign temp : agentList) {
//	    if (this.agentID != temp.agentID) {
//		double distance = this.getCurrentLocation().vectorTo(
//			temp.getCurrentLocation()).length();
//		// Point3D directionOfPreferredDirection = this.getGoal()
//		// .substract(this.getCurrentLocation()).normalize();
//		Point3D directionOfThisToNeighbor = temp.getCurrentLocation()
//			.substract(this.getCurrentLocation().normalize());
//		double cosAngle = preferredDirection
//			.dotProduct(directionOfThisToNeighbor);
//
//		if (distance <= AgentSharedData.densityRadius
//			&& cosAngle >= Math.cos(AgentSharedData.densityAngle)) {
//		    number++;
//		}
//
//		if (distance > maxDistance) {
//		    maxDistance = distance;
//		}
//	    }
//	}
//
//	rho = number
//		/ (AgentSharedData.densityRadius
//			* AgentSharedData.densityRadius * 2 * AgentSharedData.densityAngle);
//	this.capSpeed = calculateCapableSpeed();
//
//
//	this.preferredSpeed = this.calculatePreferredSpeed(capSpeed);
//
//	double distance = Math
//		.min((maxDistance > this.maxDensity) ? this.maxDensity
//			: maxDistance, 1 / Math.sqrt(this.rho));
//
//	double maxRho = 5;
//	double alpha;
//	if(this.panicLevel==0){
//	    alpha = AgentSharedData.alphaThreshold_normal;
//	}else if(this.panicLevel==1){
//	    alpha = AgentSharedData.alphaThreshold_hurry;
//	}else{
//	    alpha= AgentSharedData.alphaThreshold_emergency;
//	}
//	this.currentTTCThreshold =alpha+ Math
//		.expm1(maxRho / (maxRho - rho) - 1);
//
//    }
//
//    private double calculateCapableSpeed() {
//	// rho = calculateDensity(agentList);
//
//	double trans = 0.8;
//	double crit = 2.8;
//	double max = 5.0;
//	double v;
//	double A = 1.4;
//	if (rho <= trans) {
//	    v = A;
//	} else if (rho <= crit && rho > trans) {
//	    v = A * Math.sqrt(trans / rho);
//	} else if (rho <= max && rho > crit) {
//	    v = A * Math.sqrt(trans * crit / (max - crit))
//		    * Math.sqrt(max - rho) / rho;
//	} else {
//	    v = 0;
//	}
//	return v;
//    }
//
//    private double calculatePreferredSpeed(double cappableVel) {
//	double v = 0;
//	switch (this.panicLevel) {
//	case 0:
//	    v = cappableVel;
//	    break;
//	case 1:
//	    v = 1.5 * cappableVel;
//	    break;
//	case 2:
//	    v = 2.5 * cappableVel;
//	    break;
//	}
//	return v;
//    }
//
//    private int collisionType(AgentNewDesign other) {
//	int type = 0;
//	// get the intersection between the velocity ray and the circle of this
//	Vector<Point3D> inter = lineCircleIntersection(this
//		.getCurrentLocation(), this.getCurrentVelocity().x, this
//		.getCurrentVelocity().y, this.getCurrentLocation(),
//		this._radius);
//	if (inter.size() != 2) {
//	    System.err.println("error");
//	    System.exit(0);
//	}
//	Point3D tempPoint1 = inter.firstElement();
//	Point3D tempPoint2 = inter.lastElement();
//	// the intersection point
//	Point3D thePoint;
//	if (tempPoint1.substract(this.getCurrentLocation()).normalize()
//		.dotProduct(this.getCurrentVelocity().normalize()) >= 0) {
//	    thePoint = tempPoint1;
//	} else {
//	    thePoint = tempPoint2;
//	}
//
//	Vector<Point3D> interSection = lineCircleIntersection(this
//		.getCurrentLocation(), this.getCurrentVelocity().x, this
//		.getCurrentVelocity().y, other.getCurrentLocation(), other
//		.get_radius());
//	if (interSection.size() == 0) {
//	    type = 0;
//	} else if (interSection.size() == 1) {
//	    if ((this.getCurrentLocation()
//		    .vectorTo(interSection.firstElement()).length() + thePoint
//		    .vectorTo(interSection.firstElement()).length()) >= this._radius) {
//		type = 0;
//	    } else {
//		type = 1;
//	    }
//	} else {
//	    double length1 = this.getCurrentLocation().vectorTo(
//		    interSection.firstElement()).length();
//	    double length2 = thePoint.vectorTo(interSection.firstElement())
//		    .length();
//
//	    double length3 = this.getCurrentLocation().vectorTo(
//		    interSection.lastElement()).length();
//	    double length4 = thePoint.vectorTo(interSection.lastElement())
//		    .length();
//	    if ((length1 + length2 > this._radius)
//		    || (length3 + length4 > this._radius)) {
//		type = 1;
//	    } else {
//		type = 0;
//	    }
//	}
//	return type;
//    }
//
//    /**
//     * get the intersection between a line and a circle
//     *
//     * @param p
//     *            the point in the line
//     * @param _x
//     *            direction x of the line
//     * @param _y
//     *            direction y of the line
//     * @param center
//     *            central point of the circle
//     * @param r
//     *            radius of the circle
//     * @return
//     */
//    private Vector<Point3D> lineCircleIntersection(Point3D p, double _x,
//	    double _y, Point3D center, double r) {
//	Vector<Point3D> vec = new Vector<Point3D>();
//	if (Math.abs(_x) < 0.001) {
//	    double E = center.y * center.y - r * r + (p.x - center.x)
//		    * (p.x - center.x);
//	    double delta = 4 * center.y * center.y - 4 * E;
//	    if (delta < 0) {
//		// no intersection
//	    } else if (delta == 0) {
//		double y = 2 * center.y / 2;
//		double x = p.x;
//		vec.addElement(new Point3D(x, y, 0));
//	    } else {
//		double y1 = (2 * center.y + Math.sqrt(delta)) / 2;
//		double y2 = (2 * center.y - Math.sqrt(delta)) / 2;
//		Point3D p1 = new Point3D(p.x, y1, 0);
//		Point3D p2 = new Point3D(p.x, y2, 0);
//		vec.addElement(p1);
//		vec.addElement(p2);
//	    }
//	} else {
//	    double k = _y / _x;
//	    double b = p.y - k * p.x;
//	    double A = b - center.y;
//	    double B = 1 + k * k;
//	    double C = 2 * (k * A - center.x);
//	    double D = center.x * center.x + A * A - r * r;
//	    double delta = C * C - 4 * B * D;
//	    if (delta < 0) {
//		// no intersection
//	    } else if (delta == 0) {
//		double x = (-1) * C / (2 * B);
//		double y = k * x + b;
//		vec.addElement(new Point3D(x, y, 0));
//	    } else {
//		double x1 = ((-1) * C + Math.sqrt(delta)) / (2 * B);
//		double x2 = ((-1) * C - Math.sqrt(delta)) / (2 * B);
//		Point3D p1 = new Point3D(x1, k * x1 + b, 0);
//		Point3D p2 = new Point3D(x2, k * x2 + b, 0);
//		vec.addElement(p1);
//		vec.addElement(p2);
//	    }
//	}
//	return vec;
//    }
//
//    private Point3D changeVelocity(double scalor, double angle,
//	    Point3D collisionOne) {
//	Point3D result = new Point3D();
//	Point3D normalizedV = this.getCurrentVelocity().normalize();
//	Point3D normalizedChange1 = new Point3D(normalizedV.x * Math.cos(angle)
//		- normalizedV.y * Math.sin(angle), normalizedV.x
//		* Math.sin(angle) + normalizedV.y * Math.cos(angle), 0);
//	double theOtherAngle = 2 * Math.PI - angle;
//	Point3D normalizedChange2 = new Point3D(normalizedV.x
//		* Math.cos(theOtherAngle) - normalizedV.y
//		* Math.sin(theOtherAngle), normalizedV.x
//		* Math.sin(theOtherAngle) + normalizedV.y
//		* Math.cos(theOtherAngle), 0);
//	if (normalizedChange1.dotProduct(this.currentLocation.vectorTo(
//		collisionOne).normalize()) >= normalizedChange2
//		.dotProduct(this.currentLocation.vectorTo(collisionOne)
//			.normalize())) {
//	    result = normalizedChange2.dotProduct(this.currentVelocity.length()
//		    * scalor);
//	} else {
//	    result = normalizedChange1.dotProduct(this.currentVelocity.length()
//		    * scalor);
//	}
//
//	return result;
//    }
//
//    private Point3D collisionResponse(AgentNewDesign other) {
//	// System.out.println("============COLLISION RESPONSE===========");
//	this.paintType = 1;
//
//	int collisionType = collisionType(other);
//	if (collisionType == 0) {// side-collision
//	    this.cl = Color.green;
//	    if (this.mass == 0 && other.getMass() == 0) {// child v.s. child
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 0 && other.getMass() == 1) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F(H)
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// E+F(H)
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F(H)
//			this.selectedVelocity = new Point3D(0, 0, 0);
//			this.stopTime = 2;
//
//			this.setCurrentLocation(this.getCurrentLocation().add(
//				this.getCurrentVelocity().multiply(-1)
//					.normalize().multiply(0.1)));
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 1 && other.getMass() == 0) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// A
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(1, 0 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// A+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// B
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// A
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 1 && other.getMass() == 1) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// A+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// B+C
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection,
//			// this.neighbors, 0, 1.3 * this.capSpeed
//			// , 0, 180);
//			return changeVelocity(0.8, 20 * Math.PI / 180, other
//				.getCurrentLocation());
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	} else if (collisionType == 1) {// entire-collision
//	    this.cl = Color.red;
//	    if (this.mass == 0 && other.getMass() == 0) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//			// this.selectedVelocity = new Point3D(0, 0, 0);
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 0 && other.getMass() == 1) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F(H)
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F(H)
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// E+F(H)
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 1 && other.getMass() == 0) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+H
//
//			this.stopTime = 2;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	    if (this.mass == 1 && other.getMass() == 1) {
//		if (speedType(this.getCurrentVelocity().length()) == 0) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 1) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//		if (speedType(this.getCurrentVelocity().length()) == 2) {
//		    if (speedType(other.getCurrentVelocity().length()) == 0) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 1) {
//			// D+F
//
//			this.stopTime = 0.5;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		    if (speedType(other.getCurrentVelocity().length()) == 2) {
//			// E+F
//
//			this.stopTime = 3;
//
//			this.canMoving = false;
//			// return selectVelocityFromSectorSeperation(this,
//			// this.preferredDirection, this.neighbors, 0,
//			// this.capSpeed
//			// // 2.5
//			// , 0, 180);
//			return new Point3D();
//		    }
//		}
//	    }
//	}
//	// return selectVelocityFromSectorSeperation(this,
//	// this.preferredDirection, this.neighbors, 0,
//	// this.capSpeed
//	// // 2.5
//	// , 0, 180);
//	return new Point3D();
//    }
//
//    // fucntion for selecting a velocity to seperate two collided agents
//    private Point3D selectVelocityFromSectorSeperation(AgentNewDesign me,
//	    Point3D preferredDirection, Vector<AgentNewDesign> objectsAround,
//	    double lowerBoundSpeed, double upperBoundSpeed,
//	    double lowerBoundAngle, double upperBoundAngle) {
//	// The maximum time to collision in this sector. Initialized with -1
//	// double maxTTC = -1;
//
//	if (lowerBoundSpeed > upperBoundSpeed
//		|| lowerBoundAngle > upperBoundAngle) {
//	    try {
//		throw new Exception("Illegal Parameters");
//	    } catch (Exception e) {
//		// TODO Auto-generated catch block
//		e.toString();
//	    }
//	}
//	double speedInterval = 0.1;
//	double angleInterval = 5;
//	Point3D currentV = new Point3D();
//	double minPenalty = Double.MAX_VALUE;
//	for (double samplingSpeed = upperBoundSpeed; samplingSpeed >= lowerBoundSpeed; samplingSpeed -= speedInterval * 3) {
//	    // double samplingSpeed = upperBoundSpeed
//	    // - speedInterval;
//	    for (double samplingAngle = lowerBoundAngle; samplingAngle <= upperBoundAngle; samplingAngle += angleInterval * 4) {
//		// double samplingAngle = lowerBoundAngle + angleInterval;
//		Point3D candidateV1 = antiClockWiseRotateVector(
//			preferredDirection.multiply(samplingSpeed),
//			samplingAngle);
//
//		Point3D candidateV2 = antiClockWiseRotateVector(
//			preferredDirection.multiply(samplingSpeed), (-1)
//				* samplingAngle);
//
//		double time1 = Double.POSITIVE_INFINITY;
//		double time2 = Double.POSITIVE_INFINITY;
//		for (AgentNewDesign neighborAround : objectsAround) {
//		    Point3D vab1 = new Point3D();
//		    Point3D vab2 = new Point3D();
//
//		    vab1 = candidateV1
//			    .substract(neighborAround.currentVelocity);
//		    vab2 = candidateV2
//			    .substract(neighborAround.currentVelocity);
//
//		    double temp_time1, temp_time2;
//
//		    temp_time1 = timeTocollision(me.currentLocation, vab1,
//			    neighborAround.currentLocation,
//			    (me._radius + neighborAround._radius), _collision);
//		    temp_time1 = -Math.ceil(temp_time1
//			    / AgentSharedData.timeStep);
//		    temp_time1 -= candidateV1.dotProduct(candidateV1) / (2 * 2);
//		    // (me.anew_added_capSpeed * me.anew_added_capSpeed
//		    // * 1.3 * 1.3);
//
//		    temp_time2 = timeTocollision(me.currentLocation, vab2,
//			    neighborAround.currentLocation,
//			    (me._radius + neighborAround._radius), _collision);
//		    temp_time2 = -Math.ceil(temp_time2
//			    / AgentSharedData.timeStep);
//		    temp_time2 -= candidateV2.dotProduct(candidateV2)
//			    / (1.4 * 1.4);
//		    // (me.anew_added_capSpeed * me.anew_added_capSpeed
//		    // * 1.3 * 1.3);
//
//		    if (temp_time1 < time1) {
//			time1 = temp_time1;
//		    }
//		    if (temp_time2 < time2) {
//			time2 = temp_time2;
//		    }
//		}// End for (AgentNewDesign neighborAround
//
//		for (RectangleObstacle o : this.obsctalces) {
//		    double temp_time1 = timeTocollision(this
//			    .getCurrentLocation(), candidateV1, o.p1, o.p2,
//			    _collision);
//		    double temp_time2 = timeTocollision(this
//			    .getCurrentLocation(), candidateV2, o.p1, o.p2,
//			    _collision);
//		    if (temp_time1 < time1) {
//			time1 = temp_time1;
//		    }
//		    if (temp_time2 < time2) {
//			time2 = temp_time2;
//		    }
//		}
//		double safeFactor = 15;
//		double penalty1 = safeFactor / time1;
//		double penalty2 = safeFactor / time2;
//		Point3D tempV = penalty1 < penalty2 ? candidateV1 : candidateV2;
//		double tempPenalty = penalty1 < penalty2 ? penalty1 : penalty2;
//
//		if (tempPenalty < minPenalty) {
//		    minPenalty = tempPenalty;
//		    currentV = tempV;
//		}
//	    }// End for (int angleIndex
//	}// End for (int speedIndex
//
//	return currentV;
//    }
//
//    private Point3D antiClockWiseRotateVector(Point3D obj, double angleDegree) {
//	Point3D result = new Point3D();
//	result.x = obj.getX() * Math.cos(Math.toRadians(angleDegree))
//		- obj.getY() * Math.sin(Math.toRadians(angleDegree));
//	result.y = obj.getX() * Math.sin(Math.toRadians(angleDegree))
//		+ obj.getY() * Math.cos(Math.toRadians(angleDegree));
//	result.z = 0;
//	return result;
//    }
//
//    /**
//     * 0: normal <=1.7 1: hurry (1.7,3.0] 2: emergency >3.0
//     *
//     * @param speed
//     * @return
//     */
//    public int speedType(double speed) {
//	int type = 0;
//	if (speed <= 1.7) {
//	    type = 0;
//	}
//	if (speed > 1.7 && speed <= 3.0) {
//	    type = 1;
//	}
//	if (speed > 3.0) {
//	    type = 2;
//	}
//	return type;
//    }
//
//    public long getAgentID() {
//	return agentID;
//    }
//
//    public void setAgentID(long agentID) {
//	this.agentID = agentID;
//    }
//
//    public Vector<AgentNewDesign> getNeighbors() {
//	return neighbors;
//    }
//
//    public void setNeighbors(Vector<AgentNewDesign> neighbors) {
//	this.neighbors = neighbors;
//    }
//
//    public Point3D getGoal() {
//	return goal;
//    }
//
//    public void setGoal(Point3D goal) {
//	this.goal = goal;
//    }
//
//    public Point3D getCurrentLocation() {
//	return currentLocation;
//    }
//
//    public void setCurrentLocation(Point3D currentLocation) {
//	this.currentLocation = currentLocation;
//    }
//
//    public Point3D getCurrentVelocity() {
//	return currentVelocity;
//    }
//
//    public void setCurrentVelocity(Point3D currentVelocity) {
//	this.currentVelocity = currentVelocity;
//    }
//
//    public double getPreferredSpeed() {
//	return preferredSpeed;
//    }
//
//    public void setPreferredSpeed(double preferredSpeed) {
//	this.preferredSpeed = preferredSpeed;
//    }
//
//    // public Point3D getPrefV() {
//    // return prefV;
//    // }
//
//    public int getPanicLevel() {
//	return panicLevel;
//    }
//
//    public void setPanicLevel(int panicLevel) {
//	this.panicLevel = panicLevel;
//    }
//
//    public int getMass() {
//	return mass;
//    }
//
//    public void setMass(int mass) {
//	this.mass = mass;
//    }
//
//    public double get_radius() {
//	return _radius;
//    }
//
//    public void set_radius(double _radius) {
//	this._radius = _radius;
//    }
//
//    public void addObstacles(RectangleObstacle o) {
//	this.obsctalces.addElement(o);
//    }
//
//
//}
