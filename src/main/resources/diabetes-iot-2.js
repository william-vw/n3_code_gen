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

	hasDemographic = {};
	hasPhysicalExamination = {};
	hasTreatmentPlan;
	hasLabTest = {};
}

class PatientDemographic {
	static age = 'age';
	static obeseClassI = 'obeseClassI';

	constructor(type, hasQuantitativeValue) {
		this.type = type;
		this.hasQuantitativeValue = hasQuantitativeValue;
	}

	type;
	hasQuantitativeValue;
}

class DiabetesPhysicalExamination {
	static bmi = 'bmi';
	static historyOfPrediabetes = 'historyOfPrediabetes';

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

class DrugSubplan {
	static monotherapyPlan = 'monotherapyPlan';

	constructor(hasDrugParticipant, type) {
		this.hasDrugParticipant = hasDrugParticipant;
		this.type = type;
	}

	hasDrugParticipant;
	type;
}

class DiabetesDrug {
	static metformin = 'metformin';

	constructor(type) {
		this.type = type;
	}

	type;
}

class DiabetesLaboratoryTest {
	static hba1c = 'hba1c';
	static fpg = 'fpg';

	constructor(type, hasQuantitativeValue) {
		this.type = type;
		this.hasQuantitativeValue = hasQuantitativeValue;
	}

	type;
	hasQuantitativeValue;
}

function execute(patient) {
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi] != undefined
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.bmi].hasQuantitativeValue >= 35) {
	
		var v2 = new PatientDemographic(PatientDemographic.obeseClassI);
		patient.hasPatientProfile.hasDemographic.push(v2);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTest.hba1c] != undefined
		&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTest.hba1c].hasQuantitativeValue >= 6
		&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExamination.historyOfPrediabetes] != undefined
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.age] != undefined
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.age].hasQuantitativeValue >= 25
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.age].hasQuantitativeValue <= 59
		&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTest.fpg] != undefined
		&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTest.fpg].hasQuantitativeValue >= 110
		&& patient.hasPatientProfile.hasDemographic[PatientDemographic.obeseClassI] != undefined) {
	
		var v3 = new DrugSubplan(DrugSubplan.monotherapyPlan, DiabetesDrug.metformin);
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v3);
	}
}