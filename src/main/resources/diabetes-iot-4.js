class Patient {
	constructor(hasPatientProfile) {
		this.hasPatientProfile = hasPatientProfile;
	}

	hasPatientProfile;
}

class PatientProfile {
	constructor(hasEthnicity) {
		this.hasEthnicity = hasEthnicity;
	}

	recommendTest = {};
	hasEthnicity;
	hasPhysicalExamination = {};
	hasDemographic = {};
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

class Ethnicity {
	static highRiskEthnicity = 'highRiskEthnicity';

	static pacificIslander = 'pacificIslander';
	static africanAmerican = 'africanAmerican';
	static asianAmerican = 'asianAmerican';
	static latino = 'latino';
	static nativeAmerican = 'nativeAmerican';

	constructor(type) {
		this.type = type;
	}

	type;
}

class PatientDemographic {
	static overweight = 'overweight';

	constructor(type) {
		this.type = type;
	}

	type;
}

class Recommendation {
	static diabetesScreening = 'diabetesScreening';

	constructor(type) {
		this.type = type;
	}

	type;
}

function execute(patient) {
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi] != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi].hasQuantitativeValue >= 25
		&& patient.hasPatientProfile.hasEthnicity != undefined
		&& patient.hasPatientProfile.hasEthnicity.type != Ethnicity.asianAmerican) {
	
		var v6 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v6);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi] != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi].hasQuantitativeValue >= 23
		&& patient.hasPatientProfile.hasEthnicity.type == Ethnicity.asianAmerican) {
	
		var v7 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v7);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasEthnicity != undefined
		&& null) {
	
		var v8 = new Ethnicity(Ethnicity.highRiskEthnicity);
		patient.hasPatientProfile.hasEthnicity = v8;
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasEthnicity != undefined
		&& patient.hasPatientProfile.hasEthnicity.type == Ethnicity.highRiskEthnicity
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.overweight] != undefined) {
	
		var v9 = new Recommendation(Recommendation.diabetesScreening);
		patient.hasPatientProfile.recommendTest.push(v9);
	}
}