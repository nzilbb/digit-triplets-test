import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Trial } from './trial';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class TrialService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'trials');
    }

    readTrials(instanceId: string): Observable<Trial[]> {
        return this.http.get<Trial[]>(`${this.baseUrl}/${instanceId}`)
            .pipe(
                tap(_ => this.log('fetched trials')),
                catchError(this.handleError<Trial[]>(
                    'access trials', "Could not get trial list.", []))
            );
    }
    
    csv(instanceId: string): void {
        window.open(`${this.baseUrl}/${instanceId}?Accept=text/csv`);
    }
}
