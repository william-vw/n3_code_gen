class Patient {
	constructor(hasPatientProfile) {
		this.hasPatientProfile = hasPatientProfile;
	}

	hasPatientProfile;
}

class PatientProfile {
	constructor(hasTreatmentPlan) {
		this.hasTreatmentPlan = hasTreatmentPlan;
	}

	hasTreatmentPlan;
}

class TreatmentPlan {

	hasPart = [];
}

class TreatmentSubplan {
	static lifestyleSubplan = 'lifestyleSubplan';
	static drugSubplan = 'drugSubplan';

	constructor(label, type) {
		this.label = label;
		this.type = type;
	}

	label;
	type;
}

class BloodPressure {
	static diastolicBloodPressure = 'diastolicBloodPressure';
	static systolicBloodPressure = 'systolicBloodPressure';

	constructor(type, hasQuantitativeValue) {
		this.type = type;
		this.hasQuantitativeValue = hasQuantitativeValue;
	}

	type;
	hasQuantitativeValue;
}

function execute(sys, dias, patient) {
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& sys.type == BloodPressure.systolicBloodPressure
		&& sys.hasQuantitativeValue > 120
		&& dias.type == BloodPressure.diastolicBloodPressure
		&& dias.hasQuantitativeValue > 80) {
	
		var v0 = new TreatmentSubplan(TreatmentSubplan.lifestyleSubplan, "weight loss if indicated, \n            a Dietary Approaches to Stop Hypertension (DASH)-style eating pattern, \n            including reducing sodium and increasing potassium intake, moderation of alcohol intake, \n            and increased physical activity.");
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v0);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& sys.type == BloodPressure.systolicBloodPressure
		&& sys.hasQuantitativeValue > 140
		&& dias.type == BloodPressure.diastolicBloodPressure
		&& dias.hasQuantitativeValue > 80) {
	
		var v1 = new TreatmentSubplan(TreatmentSubplan.drugSubplan, "in addition to lifestyle therapy, \n        have prompt initiation and timely titration of pharmacologic therapy \n        to achieve blood pressure goals");
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v1);
	}
}