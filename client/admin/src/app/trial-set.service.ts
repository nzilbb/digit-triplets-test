import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { TrialSet } from './trialset';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class TrialSetService {
    
    private baseUrl = environment.baseUrl+'trialsets';
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    
    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }
    
    readTrialSets(): Observable<TrialSet[]> {
        return this.http.get<TrialSet[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched trialSets')),
                catchError(this.handleError<TrialSet[]>('readTrialSets', "Could not get trialSet list.", []))
            );
    }
    
    createTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.post<TrialSet>(this.baseUrl, trialSet, this.httpOptions)
            .pipe(
                tap((newTrialSet: TrialSet) => this.info('createTrialSet', `Added trialSet: "${newTrialSet.id}"`)),
                catchError(this.handleError<TrialSet>('createTrialSet',`Could not add "${trialSet.trials}"` ))
            );
    }
    
    updateTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.put<TrialSet>(this.baseUrl, trialSet, this.httpOptions)
            .pipe(
                tap((updatedTrialSet: TrialSet) => this.info('updateTrialSet', `Updated trialSet: "${updatedTrialSet.id}"`)),
                catchError(this.handleError<TrialSet>('updateTrialSet',`Could not update "${trialSet.id}"` ))
            );
    }
    
    deleteTrialSet(trialSet: TrialSet): Observable<TrialSet> {
        // TODO validation
        return this.http.delete<TrialSet>(`${this.baseUrl}/${trialSet.id}`)
            .pipe(
                tap(_ => this.info('deleteTrialSet', `Removed trialSet: ${trialSet.id}`)),
                catchError(this.handleError<TrialSet>('deleteTrialSet',`Could not remove "${trialSet.id}"`,
                                                      trialSet)) // return trialSet, meaning it wasn't deleted
            );
    }

    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            console.error(error);
            if (error.error && error.error.message) message += " - " + error.error.message;
            this.error(operation, message);
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }
    
    private info(operation: string, message: string) {
        this.messageService.info(message);
        this.log(`${operation} - ${message}`);
    }
    
    private error(operation: string, message: string) {
        this.messageService.error(message);
        this.log(`ERROR: ${operation} - ${message}`);
    }
    
    private log(message: string) {
        console.log(message);
    }
}
