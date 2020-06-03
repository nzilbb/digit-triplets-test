import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Attribute } from './attribute';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class AttributeService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'attributes');
    }

    readAttributes(): Observable<Attribute[]> {
        return this.http.get<Attribute[]>(`${this.baseUrl}`)
            .pipe(
                tap(_ => this.log('fetched attributes')),
                catchError(this.handleError<Attribute[]>('access parameters', "Could not get attribute list.", []))
            );
    }
    
    updateAttribute(attribute: Attribute): Observable<Attribute> {
        // TODO validation
        return this.http.put<Attribute>(this.baseUrl, attribute, this.httpOptions)
            .pipe(
                tap((updatedAttribute: Attribute) => this.info('updateAttribute', `Updated attribute: "${updatedAttribute.attribute}"`)),
                catchError(this.handleError<Attribute>('update parameters',`Could not update "${attribute.attribute}"` ))
            );
    }

}
