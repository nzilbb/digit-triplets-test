import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Text } from './text';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class TextService {
    private baseUrl = environment.baseUrl+'texts';

    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    
    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }
    
    readTexts(): Observable<Text[]> {
        return this.http.get<Text[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched texts')),
                catchError(this.handleError<Text[]>('readTexts', "Could not get text list.", []))
            );
    }
    
    getText(id: string): Observable<Text> {
        return this.http.get<Text>(`${this.baseUrl}/${id}`)
            .pipe(
                tap((t) => this.log(`fetched text "${t.id}"`)),
                catchError(this.handleError<Text>('readTexts', `Could not get text "${id}".`))
            );
    }
    
    updateText(text: Text): Observable<Text> {
        // TODO validation
        return this.http.put<Text>(this.baseUrl, text, this.httpOptions)
            .pipe(
                tap((updatedText: Text) => this.info('updateText', `Updated text: "${updatedText.label}"`)),
                catchError(this.handleError<Text>('updateText',`Could not update "${text.label}"` ))
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
