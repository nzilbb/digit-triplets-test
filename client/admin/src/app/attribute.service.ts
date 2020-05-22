import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Attribute } from './attribute';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class AttributeService {
    private baseUrl = environment.baseUrl+'attributes';
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json;charset=UTF-8' })
    };
    
    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }

    readAttributes(): Observable<Attribute[]> {
        return this.http.get<Attribute[]>(`${this.baseUrl}`)
            .pipe(
                tap(_ => this.log('fetched attributes')),
                catchError(this.handleError<Attribute[]>('readAttributes', "Could not get attribute list.", []))
            );
    }
    
    updateAttribute(attribute: Attribute): Observable<Attribute> {
        // TODO validation
        return this.http.put<Attribute>(this.baseUrl, attribute, this.httpOptions)
            .pipe(
                tap((updatedAttribute: Attribute) => this.info('updateAttribute', `Updated attribute: "${updatedAttribute.attribute}"`)),
                catchError(this.handleError<Attribute>('updateAttribute',`Could not update "${attribute.attribute}"` ))
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
