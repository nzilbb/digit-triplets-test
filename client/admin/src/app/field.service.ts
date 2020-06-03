import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Field } from './field';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class FieldService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'fields');
    }
    
    readFields(): Observable<Field[]> {
        return this.http.get<Field[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched fields')),
                catchError(this.handleError<Field[]>(
                    'access fields', "Could not get field list.", []))
            );
    }

    createField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.post<Field>(this.baseUrl, field, this.httpOptions)
            .pipe(
                tap((newField: Field) => this.info(
                    'createField', `Added field: "${newField.field}"`)),
                catchError(this.handleError<Field>(
                    'create fields',`Could not add "${field.field}"` ))
            );
    }

    updateField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.put<Field>(this.baseUrl, field, this.httpOptions)
            .pipe(
                tap((updatedField: Field) => this.info(
                    'updateField', `Updated field: "${updatedField.field}"`)),
                catchError(this.handleError<Field>(
                    'update fields',`Could not update "${field.field}"` ))
            );
    }
    
    deleteField(field: Field): Observable<Field> {
        // TODO validation
        return this.http.delete<Field>(`${this.baseUrl}/${field.field}`)
            .pipe(
                tap(_ => this.info('deleteField', `Removed field: ${field.field}`)),
                catchError(this.handleError<Field>(
                    'delete fields',`Could not remove "${field.field}"`,
                    field)) // return field, meaning it wasn't deleted
            );
    }    
}
