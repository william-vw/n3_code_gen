class Patient {
	constructor(hasPatientProfile) {
		this.hasPatientProfile = hasPatientProfile;
	}

	hasPatientProfile;
}

class PatientProfile {

	hasEthnicity = [];
	hasPhysicalExamination = [];
	hasDemographic = [];
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

function execute(patient) {
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.type == DiabetesPhysicalExamination.bmi)
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.hasQuantitativeValue >= 25)) {
	
		var v0 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v0);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.type == DiabetesPhysicalExamination.bmi)
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.hasQuantitativeValue >= 23)) {
	
		var v1 = new PatientDemographic(PatientDemographic.overweight);
		patient.hasPatientProfile.hasDemographic.push(v1);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.type == PatientDemographic.overweight)
		&& patient.hasPatientProfile.hasEthnicity.some((e) => e.type == Ethnicity.highRiskEthnicity)) {
	
		
	}
}