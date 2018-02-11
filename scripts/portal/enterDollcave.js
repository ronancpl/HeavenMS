function enter(pi) {
        if(pi.isQuestCompleted(20730) || pi.isQuestCompleted(21734)) {  // puppeteer defeated, newfound secret path
            pi.playPortalSound(); pi.warp(105040201,2);
            return true;
        }
    
	pi.openNpc(1063011, "PupeteerPassword");
	return false;
}