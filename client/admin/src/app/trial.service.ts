import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Trial } from './trial';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class TrialService {
    private baseUrl = environment.baseUrl+'trials';
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json;charset=UTF-8' })
    };

    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }

    readTrials(instanceId: string): Observable<Trial[]> {
        return this.http.get<Trial[]>(`${this.baseUrl}/${instanceId}`)
            .pipe(
                tap(_ => this.log('fetched trials')),
                catchError(this.handleError<Trial[]>('readTrials', "Could not get trial list.", []))
            );
    }
    
    csv(instanceId: string): void {
        window.open(`${this.baseUrl}/${instanceId}?Accept=text/csv`);
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
