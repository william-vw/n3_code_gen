class Patient {
	constructor(hasPatientProfile) {
		this.hasPatientProfile = hasPatientProfile;
	}

	hasPatientProfile;
}

class PatientProfile {
	constructor(hasTreatmentPlan, hasDiagnosis) {
		this.hasTreatmentPlan = hasTreatmentPlan;
		this.hasDiagnosis = hasDiagnosis;
	}

	hasDemographic = {};
	hasTreatmentPlan;
	hasDiagnosis;
}

class PatientDemographic {
	static overweight = 'overweight';

	constructor(type) {
		this.type = type;
	}

	type;
}

class DiabetesPhysicalExamination {
	static bmi = 'bmi';

	constructor(type, hasQuantitativeValue) {
		this.type = type;
		this.hasQuantitativeValue = hasQuantitativeValue;
	}

	type;
	hasQuantitativeValue;
}

class TreatmentPlan {

	hasPart = {};
}

class TreatmentSubplan {
	static lifestyleSubplan = 'lifestyleSubplan';

	constructor(label, type) {
		this.label = label;
		this.type = type;
	}

	label;
	type;
}

class DiabetesDiagnosis {
	constructor(hasDiabetesType) {
		this.hasDiabetesType = hasDiabetesType;
	}

	hasDiabetesType;
}

class DiabetesMellitus {
	static type2DiabetesMellitus = 'type2DiabetesMellitus';

	constructor(type) {
		this.type = type;
	}

	type;
}

function execute(patient, exam) {
	if (patient.hasPatientProfile != undefined
		&& exam.type == DiabetesPhysicalExamination.bmi
		&& exam.hasQuantitativeValue >= 25) {
	
		var v0 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v0);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& patient.hasPatientProfile.hasDiagnosis != undefined
		&& patient.hasPatientProfile.hasDiagnosis.hasDiabetesType != undefined
		&& patient.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitus.type2DiabetesMellitus
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.overweight] != undefined) {
	
		var v1 = new TreatmentSubplan(TreatmentSubplan.lifestyleSubplan, "Management and reduction of weight is important");
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v1);
	}
}