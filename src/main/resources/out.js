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

	hasDemographic = [];
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

	hasPart = [];
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

function doSomething(exam, patient) {
	if (patient.hasPatientProfile != undefined
		&& exam.hasQuantitativeValue >= 25
		&& exam.type == DiabetesPhysicalExamination.bmi) {
	
		var v0 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v0);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& patient.hasPatientProfile.hasDiagnosis != undefined
		&& patient.hasPatientProfile.hasDiagnosis.hasDiabetesType != undefined
		&& patient.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitus.type2DiabetesMellitus
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.type == PatientDemographic.overweight)) {
	
		var v1 = new TreatmentSubplan("Management and reduction of weight is important", TreatmentSubplan.lifestyleSubplan);
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v1);
	}
}