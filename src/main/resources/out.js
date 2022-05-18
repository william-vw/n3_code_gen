class DiabetesPhysicalExamination {
	static bmi = 'bmi';

	constructor(type, hasQuantitativeValue) {
		this.type = type;
		this.hasQuantitativeValue = hasQuantitativeValue;
	}

	type;
	hasQuantitativeValue;
}

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

function doSomething(exam, p) {
	if (exam.hasQuantitativeValue >= 25
		&& exam.type == DiabetesPhysicalExamination.bmi
		&& p.hasPatientProfile != undefined) {
	
		var v0 = new PatientDemographic(PatientDemographic.overweight);
		p.hasPatientProfile.hasDemographic.push(v0);
	}
	
	if (p.hasPatientProfile != undefined
		&& p.hasPatientProfile.hasTreatmentPlan != undefined
		&& p.hasPatientProfile.hasDiagnosis != undefined
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType != undefined
		&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitus.type2DiabetesMellitus
		&& p.hasPatientProfile.hasDemographic.some((e) => e.type == PatientDemographic.overweight)) {
	
		var v1 = new TreatmentSubplan("Management and reduction of weight is important", TreatmentSubplan.lifestyleSubplan);
		p.hasPatientProfile.hasTreatmentPlan.hasPart.push(v1);
	}
}