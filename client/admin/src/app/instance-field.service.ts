import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { InstanceField } from './instancefield';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class InstanceFieldService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'instancefields');
    }
    
    getFieldValue(instance_id: string, field:string): Observable<InstanceField> {
        return this.http.get<InstanceField>(`${this.baseUrl}/${instance_id}/${field}`)
            .pipe(
                tap(fieldValue => this.log(`fetched value ${fieldValue.instance_id}/${fieldValue.field} = ${fieldValue.value}`)),
                catchError(this.handleError<InstanceField>('getFieldValue', `Could not get value of ${instance_id}/${field}`, { instance_id: instance_id, field: field, value: "" } as InstanceField))
            );
    }
    
    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            if (error.status == 403) {
                this.error(operation, "Sorry, you don't have permission to get field values");
            } else if (error.status != 404) {
                console.error(error);
                if (error.error && error.error.message) message += " - " + error.error.message;
                this.error(operation, message);
            }
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }
}

