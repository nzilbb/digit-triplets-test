import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Instance } from './instance';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class InstanceService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'instances');
    }

    readInstances(page: number, length: number): Observable<Instance[]> {
        return this.http.get<Instance[]>(`${this.baseUrl}?p=${page}&l=${length}`)
            .pipe(
                tap(_ => this.log('fetched instances')),
                catchError(this.handleError<Instance[]>(
                    'access instances', "Could not get instance list.", []))
            );
    }

    csv(): void {
        window.open(`${this.baseUrl}?Accept=text/csv`);
    }
}
