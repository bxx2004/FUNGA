rootProject.name = "LifePhoton"
include("module:authentication")
include("common")
include("module:file-management")
include("module:genome")
include("module:mating-type-imputation")
include("tester")
include("module:funga")
findProject(":module:funga")?.name = "funga"

include("module:homepage")
include("module:ai-assistant")