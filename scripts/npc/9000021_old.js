/*
 * Aran / Cygnus Job Advancer
 * v83 MoopleDEV Rev 116
 * Author: ShEtY
 */

var status = -1;
var ask = "Oh, I see you've grown stronger! Do you wish to job advance?";
var job;
var koc = new Array();

function start() {
   cm.sendNext("Hey #e#h ##n, I'm the Aran & Cygnus Job Advancer.");
}

function action(m, t, s) {
    status++;
    if (m != 1) {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (cm.getJobId() < 1000 ||cm.getJobId() % 10 == 2) {
            cm.dispose();
        } else if (cm.getJobId() % 10 == 1 && cm.getJobId() < 2000) {
            cm.dispose();
        } else if (cm.getJobId() % 1000 == 0 && cm.getJobId() != 0 && cm.getLevel() >= 10) {
            if (cm.getJobId() == 1000) {
                for (var i = 1; i < 6; i++)
                koc.push(cm.getJobId() + 100 * i);
                var list = "What class of Cygnus Knight do you wish to advance to?";
                for (var k = 0; k < koc.length; k++)
                list += "\r\n#L" + k + "#" + cm.getJobName(koc[k]) + "#l";
                cm.sendSimple(list);
            } else if (cm.getJobId() == 2000) {
                job = cm.getJobId() + 100;
                cm.sendYesNo(ask);
            }
        else
            cm.dispose();
        } else if (cm.getJobId() % 1000 != 0) {
            if (cm.getJobId() % 100 == 0 && cm.getLevel() >= 30) {
                job = cm.getJobId() + 10;
                cm.sendYesNo(ask);
            } else if (cm.getJobId() % 10 == 0 && cm.getLevel() >= 70) {
                job = cm.getJobId() + 1;
                cm.sendYesNo(ask);
            } else if (cm.getJobId() % 10 == 1 && cm.getJobId() >= 2000 && cm.getLevel() >= 120) {
                job = cm.getJobId() + 1;
                cm.sendYesNo(ask);
            }
        }
    } else if (status == 1) {
        if (cm.getJobId() != 1000) {
            if (cm.getJobId() == 2000) {
                cm.changeJobById(job);
                cm.maxMastery();
                cm.resetStats();
                cm.dispose();
            } else {
                cm.changeJobById(job);
                cm.dispose();
            }
        } else if (cm.getJobId() == 1000) {
            cm.changeJobById(koc[s]);
            cm.resetStats();
            cm.dispose();
        }
    }
}  