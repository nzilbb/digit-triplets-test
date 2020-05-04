import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Field } from './field';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class FieldService {
    private baseUrl = environment.baseUrl+'fields';
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    
    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }
    
    readFields(): Observable<Field[]> {
        return this.http.get<Field[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched fields')),
                catchError(this.handleError<Field[]>('readFields', "Could not get field list.", []))
            );
    }
    
    createField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.post<Field>(this.baseUrl, field, this.httpOptions)
            .pipe(
                tap((newField: Field) => this.info('createField', `Added field: "${newField.field}"`)),
                catchError(this.handleError<Field>('createField',`Could not add "${field.field}"` ))
            );
    }

    updateField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.put<Field>(this.baseUrl, field, this.httpOptions)
            .pipe(
                tap((updatedField: Field) => this.info('updateField', `Updated field: "${updatedField.field}"`)),
                catchError(this.handleError<Field>('updateField',`Could not update "${field.field}"` ))
            );
    }
    
    deleteField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.delete<Field>(`${this.baseUrl}/${field.field}`)
            .pipe(
                tap(_ => this.info('deleteField', `Removed field: ${field.field}`)),
                catchError(this.handleError<Field>('deleteField',`Could not remove "${field.field}"`,
                                                   field)) // return field, meaning it wasn't deleted
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
