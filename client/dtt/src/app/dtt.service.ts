import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { Text } from './text';
import { Instance } from './instance';

@Injectable({
  providedIn: 'root'
})
export class DttService {
    private baseUrl = environment.baseUrl;
    private instance = {} as Instance;

    constructor(
        private http: HttpClient,
        private router: Router
    ) { }

    getText(id: string): Observable<Text> {
        return this.http.get<Text>(`${this.baseUrl}/text/${id}`)
            .pipe(
                tap((text) => this.log(`fetched text "${text.id}"`)),
                catchError(this.handleError<Text>('getText', `Could not get text "${id}".`))
            );
    }

    nextAfterText(id: string): void {
        if (id === "introduction") {
            this.router.navigateByUrl('/sound-check');
        }
    }
    
    start(mode: string): void {
        this.instance.mode = mode;
        this.router.navigateByUrl('/sound-check');
    }

    volumeCheckUrl(): string {
        return `${this.baseUrl}/mp3/DTT${this.instance.mode}/sound-check.mp3`;
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
        this.log(`${operation} - ${message}`);
    }
    
    private error(operation: string, message: string) {
        console.error(`ERROR: ${operation} - ${message}`);
    }
    
    private log(message: string) {
        console.log(message);
    }

}
