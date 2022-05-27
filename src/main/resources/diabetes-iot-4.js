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
	static pacificIslander = 'pacificIslander';
	static highRiskEthnicity = 'highRiskEthnicity';
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
	
		var v0 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic[v0.type] = v0;
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi] != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi].hasQuantitativeValue >= 23
		&& patient.hasPatientProfile.hasEthnicity.type == Ethnicity.asianAmerican) {
	
		var v1 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic[v1.type] = v1;
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasEthnicity != undefined
		&& (patient.hasPatientProfile.hasEthnicity.type == Ethnicity.africanAmerican
		|| patient.hasPatientProfile.hasEthnicity.type == Ethnicity.latino
		|| patient.hasPatientProfile.hasEthnicity.type == Ethnicity.nativeAmerican
		|| patient.hasPatientProfile.hasEthnicity.type == Ethnicity.asianAmerican
		|| patient.hasPatientProfile.hasEthnicity.type == Ethnicity.pacificIslander)) {
	
		patient.hasPatientProfile.hasEthnicity.type = Ethnicity.highRiskEthnicity;
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.overweight] != undefined
		&& patient.hasPatientProfile.hasEthnicity.type == Ethnicity.highRiskEthnicity) {
	
		var v2 = new Recommendation(Recommendation.diabetesScreening);
		patient.hasPatientProfile.recommendTest[v2.type] = v2;
	}
}