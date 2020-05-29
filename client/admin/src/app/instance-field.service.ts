import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { InstanceField } from './instancefield';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class InstanceFieldService {
    private baseUrl = environment.baseUrl+'instancefields';
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json;charset=UTF-8' })
    };
    
    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }
    
    getFieldValue(instance_id: string, field:string): Observable<InstanceField> {
        return this.http.get<InstanceField>(`${this.baseUrl}/${instance_id}/${field}`)
            .pipe(
                tap(fieldValue => this.log(`fetched value ${fieldValue.instance_id}/${fieldValue.field} = ${fieldValue.value}`)),
                catchError(this.handleError<InstanceField>('getFieldValue', `Could not get value of ${instance_id}/${field}`, { instance_id: instance_id, field: field, value: "" } as InstanceField))
            );
    }

    handleError<T>(operation = 'operation', message = "ERROR", result?:T) {
        return (error: any): Observable<T> => {
            if (error.status != 404) {
                console.error(error);
                if (error.error && error.error.message) message += " - " + error.error.message;
                this.error(operation, message);
            }
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }
    
    private info(operation: string, message: string) {
        this.log(`${operation} - ${message}`);
    }
    
    private error(operation: string, message: string) {
        this.log(`ERROR: ${operation} - ${message}`);
    }
    
    private log(message: string) {
        console.log(message);
    }
}
