// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot {
	// Declares a state variable `message` of type `string`.
	// State variables are variables whose values are permanently stored in contract storage. The keyword `public` makes variables accessible from outside a contract and creates a function that other contracts or clients can call to access the value.
	string public message;
	
	// Similar to many class-based object-oriented languages, a constructor is a special function that is only executed upon contract creation.
	// Constructors are used to initialize the contract's data. Learn more:https://solidity.readthedocs.io/en/v0.5.10/contracts.html#constructors
	constructor(string memory initMessage) {
	
		// Accepts a string argument `initMessage` and sets the value into the contract's `message` storage variable).
		message = initMessage;
	}
	
	enum TreatmentSubplanConstants{ LifestyleSubplan }
	
	struct TreatmentSubplan {
		string label;
		TreatmentSubplanConstants hasType;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminationConstants{ Bmi }
	
	struct DiabetesPhysicalExamination {
		DiabetesPhysicalExaminationConstants hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum DiabetesMellitusConstants{ Type2DiabetesMellitus }
	
	struct DiabetesMellitus {
		DiabetesMellitusConstants hasType;
		bool exists;
	}
	
	struct TreatmentPlan {
		mapping(TreatmentSubplanConstants => TreatmentSubplan) hasPart;
		bool exists;
	}
	
	struct DiabetesDiagnosis {
		DiabetesMellitus hasDiabetesType;
		bool exists;
	}
	
	struct Patient {
		PatientProfile hasPatientProfile;
		bool exists;
	}
	
	struct PatientProfile {
		mapping(PatientDemographicConstants => PatientDemographic) hasDemographic;
		mapping(DiabetesPhysicalExaminationConstants => DiabetesPhysicalExamination) hasPhysicalExamination;
		TreatmentPlan hasTreatmentPlan;
		DiabetesDiagnosis hasDiagnosis;
		bool exists;
	}
	
	enum PatientDemographicConstants{ Overweight }
	
	struct PatientDemographic {
		PatientDemographicConstants hasType;
		bool exists;
	}
	
	function doSomething(p, exam) {
		if (p.hasPatientProfile != 0
			&& null
			&& null
			&& p.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExaminationConstants.Bmi].exists
			&& exam.hasQuantitativeValue != 0
			&& exam.hasQuantitativeValue >= 25
			&& exam.hasType == DiabetesPhysicalExaminationConstants.Bmi) {
		
			PatientDemographic memory v0 = PatientDemographic({ hasType: PatientDemographicConstants.Overweight, exists: true });
			p.hasPatientProfile.hasDemographic[v0.hasType] = v0;
		}
		
		if (p.hasPatientProfile != 0
			&& p.hasPatientProfile.hasTreatmentPlan != 0
			&& p.hasPatientProfile.hasDiagnosis != 0
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType != 0
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.hasType == DiabetesMellitusConstants.Type2DiabetesMellitus
			&& p.hasPatientProfile.hasDemographic[PatientDemographicConstants.Overweight].exists) {
		
			TreatmentSubplan memory v1 = TreatmentSubplan({ label: "Management and reduction of weight is important", hasType: TreatmentSubplanConstants.LifestyleSubplan, exists: true });
			p.hasPatientProfile.hasTreatmentPlan.hasPart[v1.hasType] = v1;
		}
	}
	
	// A public function that accepts a string argument and updates the `message` storage variable.
	function update(string memory newMessage) public {
		message = newMessage;
	}}