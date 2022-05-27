pragma solidity >=0.4.16;

contract {{physician}} {

    // events
    
    event PatientOverweightEvent(uint time);
    event TreatmentSubplanEvent(uint time, string label);

    // structs

    // merges all contents of PatientProfile and TreatmentPlan due to error:
    // TypeError: Types in storage containing (nested) mappings cannot be assigned to.
    // (see Test3)

    struct Patient {
        // - PatientProfile
		mapping(PatientDemographicConstants => PatientDemographic) hasDemographic;
        mapping(DiabetesPhysicalExaminationConstants => DiabetesPhysicalExamination) hasExam;
		DiabetesDiagnosis hasDiagnosis;

        // - TreatmentPlan
		mapping(TreatmentSubplanConstants => TreatmentSubplan) hasPart;

		bool exists;
	}

    enum PatientDemographicConstants{ Overweight }
	
	struct PatientDemographic {
		PatientDemographicConstants hasType;
		bool exists;
	}

    enum DiabetesMellitusConstants{ Type2DiabetesMellitus }
	
	struct DiabetesMellitus {
		DiabetesMellitusConstants hasType;
		bool exists;
	}
	
	struct DiabetesDiagnosis {
		DiabetesMellitus hasDiabetesType;
		bool exists;
	}

    enum DiabetesPhysicalExaminationConstants{ Bmi }
	
	struct DiabetesPhysicalExamination {
        //PatientProfile isPhysicalExaminationOf;
		DiabetesPhysicalExaminationConstants hasType;
		int hasQuantitativeValue;
		bool exists;
	}
    
    enum TreatmentSubplanConstants{ LifestyleSubplan }
	
	struct TreatmentSubplan {
		string label;
		TreatmentSubplanConstants hasType;
		bool exists;
	}

    // storage containers

    // TODO these mappings are needed to cope with error:
    // "TypeError: Struct containing a (nested) mapping cannot be constructed"
    // (see https://ethereum.stackexchange.com/questions/87451/solidity-error-struct-containing-a-nested-mapping-cannot-be-constructed)

    // uint numPatients = 0;
    // mapping(uint => Patient) patients;

    mapping(address => Patient) patients;

    function createPatient() public {
        DiabetesMellitus memory dm = DiabetesMellitus({ hasType: DiabetesMellitusConstants.Type2DiabetesMellitus, exists: true });
        DiabetesDiagnosis memory diagnosis = DiabetesDiagnosis({ hasDiabetesType: dm, exists: true});
        
        Patient storage p = patients[msg.sender];
        p.hasDiagnosis = diagnosis;
    }
    
    // e.g., pass [ "0", "27", "true" ] as input
    // (cannot pass parameter with (nested) mapping)
    // function doSomething(int hasQuant, bool exists) public returns (uint) {    
    function doSomething(DiabetesPhysicalExamination memory exam) public returns (uint){
        // DiabetesPhysicalExaminationConstants hasType = DiabetesPhysicalExaminationConstants.Bmi;
        // DiabetesPhysicalExamination memory exam = DiabetesPhysicalExamination(hasType, hasQuant, exists);
    // function doSomething(string[] memory exam) public {
        Patient storage patient = patients[msg.sender];
        // resetting prior inferred "overweight" demographic here
        delete patient.hasDemographic[PatientDemographicConstants.Overweight];

        if (exam.hasQuantitativeValue != 0
			&& exam.hasQuantitativeValue >= 25
			&& exam.hasType == DiabetesPhysicalExaminationConstants.Bmi) {
            
			PatientDemographic memory v0 = PatientDemographic({ exists: true, 
                hasType: PatientDemographicConstants.Overweight });

            patient.hasDemographic[v0.hasType] = v0;

            emit PatientOverweightEvent(block.timestamp);
            return 2;
        }

        if (patient.hasDiagnosis.exists
			&& patient.hasDiagnosis.hasDiabetesType.exists
            && patient.hasDiagnosis.hasDiabetesType.hasType == DiabetesMellitusConstants.Type2DiabetesMellitus
			&& patient.hasDemographic[PatientDemographicConstants.Overweight].exists) {
		
			TreatmentSubplan memory v2 = TreatmentSubplan({ 
                label: "Management and reduction of weight is important",
                hasType: TreatmentSubplanConstants.LifestyleSubplan,
                exists: true
            });

            patient.hasPart[v2.hasType] = v2;

            emit TreatmentSubplanEvent(block.timestamp, v2.label);
            return 3;
		}
        return 4;
    }
}