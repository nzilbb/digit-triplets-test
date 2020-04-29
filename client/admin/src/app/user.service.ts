import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';

import { User } from './user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
    
    // private baseUrl = 'users';
    private baseUrl = 'http://localhost:8080/digit-triplets-test/admin/users';
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };

    constructor(
        private http: HttpClient
    ) { }

    getUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched users')),
                catchError(this.handleError<User[]>('getUsers', []))
            );
    }

    addUser(user: User): Observable<User> {
        // TODO validation
        return this.http.post<User>(this.baseUrl, user, this.httpOptions)
            .pipe(
                tap((newUser: User) => this.log(`Added user: ${newUser.user}`)),
                catchError(this.handleError<User>('addUser'))
            );
    }

    handleError<T>(operation = 'operation', result?:T) {
        return (error: any): Observable<T> => {
            console.error(error);
            this.log(`${operation} failed: ${error.message}`);
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

    /** Log a message */
    private log(message: string) {
        //this.messageService.add(`UserService: ${message}`); TODO
        console.log(message);
    }
}
