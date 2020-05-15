import { Component, OnInit, Input } from '@angular/core';
import { TrialSet } from '../trialset';
import { TrialSetService } from '../trial-set.service';

@Component({
    selector: 'app-trial-sets',
    templateUrl: './trial-sets.component.html',
    styleUrls: ['./trial-sets.component.css']
})
export class TrialSetsComponent implements OnInit {
    trialSets: TrialSet[];
    changed = false;
    
    constructor(
        private trialSetService: TrialSetService
    ) {}
    
    ngOnInit(): void {
        this.readTrialSets();
    }
    
    createTrialSet(trials: string) {
        this.trialSetService.createTrialSet({ trials } as TrialSet)
            .subscribe(trialSet => {
                // add to the model/view
                if (trialSet) this.trialSets.push(trialSet); });
    }
    
    readTrialSets(): void {
        this.trialSetService.readTrialSets()
            .subscribe(trialSets => this.trialSets = trialSets);
    }
    
    updateTrialSet(trialSet: TrialSet) {
        this.trialSetService.updateTrialSet(trialSet)
            .subscribe(trialSet => {
                trialSet.changed = false; // this may have been passed back unchanged
                // update the model with the trialSet returned
                const i = this.trialSets.findIndex(u => { return u.id == trialSet.id; });
                if (i >= 0) this.trialSets[i] = trialSet;
                this.updateChangedFlag();
            });
    }
    
    deleteTrialSet(trialSet: TrialSet) {
        this.trialSetService.deleteTrialSet(trialSet)
            .subscribe(returnedTrialSet => {
                // when removal is successful, returnedTrialSet == null
                if (!returnedTrialSet) {
                    // remove from the model/view
                    this.trialSets = this.trialSets.filter(u => { return u !== trialSet;});
                    this.updateChangedFlag();
                }});
    }
    
    onChange(trialSet: TrialSet) {
        trialSet.changed = this.changed = true;        
    }
    
    updateChangedTrialSets() {
        this.trialSets
            .filter(u => u.changed)
            .forEach(u => this.updateTrialSet(u));
    }

    updateChangedFlag() {
        this.changed = false;
        for (let trialSet of this.trialSets) {
            if (trialSet.changed) {
                this.changed = true;
                break; // only need to find one
            }
        } // next trialSet
    }
}
