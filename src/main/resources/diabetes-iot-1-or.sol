// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot1 {
	string public message;
	
	constructor(string memory initMessage) {
		message = initMessage;
	}
	
	
	event NewTreatmentSubPlan(uint time);
	
	
	struct Patient {
		mapping(TreatmentSubplans => TreatmentSubplan) hasPart;
		mapping(PatientDemographics => PatientDemographic) hasDemographic;
		DiabetesDiagnosis hasDiagnosis;
		bool exists;
	}
	
	enum PatientDemographics{ Overweight }
	
	struct PatientDemographic {
		PatientDemographics hasType;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminations{ Bmi }
	
	struct DiabetesPhysicalExamination {
		DiabetesPhysicalExaminations hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum TreatmentSubplans{ LifestyleSubplan }
	
	struct TreatmentSubplan {
		string label;
		TreatmentSubplans hasType;
		bool exists;
	}
	
	struct DiabetesDiagnosis {
		DiabetesMellitus hasDiabetesType;
		bool exists;
	}
	
	enum DiabetesMellituses{ Type2DiabetesMellitus }
	
	struct DiabetesMellitus {
		DiabetesMellituses hasType;
		bool exists;
	}
	
	
	mapping(address => Patient) patients;
	
	function execute(DiabetesPhysicalExamination memory exam) {
		Patient storage patient = patients[msg.sender];
	
		if (exam.hasType == DiabetesPhysicalExaminations.Bmi
			&& exam.hasQuantitativeValue >= 25) {
		
			PatientDemographic memory v0 = PatientDemographic({ hasType: PatientDemographics.Overweight, exists: true });
			patient.hasDemographic[v0.hasType] = v0;
		}
		
		if (patient.hasDiagnosis.exists
			&& patient.hasDiagnosis.hasDiabetesType.exists
			&& patient.hasDiagnosis.hasDiabetesType.hasType == DiabetesMellituses.Type2DiabetesMellitus
			&& patient.hasDemographic[PatientDemographics.Overweight].exists) {
		
			TreatmentSubplan memory v1 = TreatmentSubplan({ hasType: TreatmentSubplans.LifestyleSubplan, label: "Management and reduction of weight is important", exists: true });
			patient.hasPart[v1.hasType] = v1;
		
			emit NewTreatmentSubPlan(block.timestamp);
		}
	}
	
	function update(string memory newMessage) public {
		message = newMessage;
	}}