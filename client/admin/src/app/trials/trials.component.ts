import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { Trial } from '../trial';
import { TrialService } from '../trial.service';

@Component({
    selector: 'app-trials',
    templateUrl: './trials.component.html',
    styleUrls: ['./trials.component.css']
})
export class TrialsComponent implements OnInit {
    instanceId: string;
    trials: Trial[];
    
    constructor(
        private route: ActivatedRoute,
        private location: Location,                
        private trialService: TrialService
    ) { }
    
    ngOnInit(): void {
        this.readTrials();    
    }
    
    readTrials(): void {
        this.instanceId = this.route.snapshot.paramMap.get('instanceId');
        this.trialService.readTrials(this.instanceId)
            .subscribe((trials) => {
                this.trials = trials;
            });
    }    
}
