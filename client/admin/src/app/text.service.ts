import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Text } from './text';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class TextService extends Service {

    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'texts');
    }
    
    readTexts(): Observable<Text[]> {
        return this.http.get<Text[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched texts')),
                catchError(this.handleError<Text[]>(
                    'access texts', "Could not get text list.", []))
            );
    }
    
    getText(id: string): Observable<Text> {
        return this.http.get<Text>(`${this.baseUrl}/${id}`)
            .pipe(
                tap((t) => this.log(`fetched text "${t.id}"`)),
                catchError(this.handleError<Text>(
                    'access a text', `Could not get text "${id}".`))
            );
    }
    
    updateText(text: Text): Observable<Text> {
        // TODO validation
        return this.http.put<Text>(this.baseUrl, text, this.httpOptions)
            .pipe(
                tap((updatedText: Text) => this.info(
                    'update texts', `Updated text: "${updatedText.label}"`)),
                catchError(this.handleError<Text>(
                    'update texts',`Could not update "${text.label}"` ))
            );
    }
}
