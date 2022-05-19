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

	hasPhysicalExamination = [];
	hasDemographic = [];
	hasTreatmentPlan;
	hasLabTest = [];
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

class TreatmentPlan {

	hasPart = [];
}

class DrugSubplan {
	constructor(hasDrugParticipant) {
		this.hasDrugParticipant = hasDrugParticipant;
	}

	hasDrugParticipant;
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
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.type == DiabetesPhysicalExamination.bmi)
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.hasQuantitativeValue >= 35)) {
	
		var v0 = new PatientDemographic(PatientDemographic.obeseClassI);
		patient.hasPatientProfile.hasDemographic.push(v0);
	}
	
	if (patient.hasPatientProfile != undefined
		&& patient.hasPatientProfile.hasTreatmentPlan != undefined
		&& patient.hasPatientProfile.hasLabTest.some((e) => e.type == DiabetesLaboratoryTest.hba1c)
		&& patient.hasPatientProfile.hasLabTest.some((e) => e.hasQuantitativeValue >= 6)
		&& patient.hasPatientProfile.hasPhysicalExamination.some((e) => e.type == DiabetesPhysicalExamination.historyOfPrediabetes)
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.type == PatientDemographic.age)
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.hasQuantitativeValue >= 25)
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.hasQuantitativeValue <= 59)
		&& patient.hasPatientProfile.hasLabTest.some((e) => e.type == DiabetesLaboratoryTest.fpg)
		&& patient.hasPatientProfile.hasLabTest.some((e) => e.hasQuantitativeValue >= 110)
		&& patient.hasPatientProfile.hasDemographic.some((e) => e.type == PatientDemographic.obeseClassI)) {
	
		var v1 = new DrugSubplan(DiabetesDrug.metformin);
		patient.hasPatientProfile.hasTreatmentPlan.hasPart.push(v1);
	}
}