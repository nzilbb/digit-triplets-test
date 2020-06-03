import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { TrialSet } from './trialset';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class TrialSetService extends Service {    
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'trialsets');
    }
    
    readTrialSets(): Observable<TrialSet[]> {
        return this.http.get<TrialSet[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched trialSets')),
                catchError(this.handleError<TrialSet[]>(
                    'access trial sets', "Could not get trialSet list.", []))
            );
    }
    
    createTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.post<TrialSet>(this.baseUrl, trialSet, this.httpOptions)
            .pipe(
                tap((newTrialSet: TrialSet) => this.info('createTrialSet', `Added trialSet: "${newTrialSet.id}"`)),
                catchError(this.handleError<TrialSet>(
                    'create trial sets',`Could not add "${trialSet.trials}"` ))
            );
    }
    
    updateTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.put<TrialSet>(this.baseUrl, trialSet, this.httpOptions)
            .pipe(
                tap((updatedTrialSet: TrialSet) => this.info(
                    'updateTrialSet', `Updated trialSet: "${updatedTrialSet.id}"`)),
                catchError(this.handleError<TrialSet>(
                    'update trial sets',`Could not update "${trialSet.id}"` ))
            );
    }
    
    deleteTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.delete<TrialSet>(`${this.baseUrl}/${trialSet.id}`)
            .pipe(
                tap(_ => this.info('deleteTrialSet', `Removed trialSet: ${trialSet.id}`)),
                catchError(this.handleError<TrialSet>(
                    'delete trial sets',`Could not remove "${trialSet.id}"`,
                    trialSet)) // return trialSet, meaning it wasn't deleted
            );
    }
}
