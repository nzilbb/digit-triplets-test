import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { User } from './user';
import { MessageService } from './message.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
    
    private baseUrl = environment.baseUrl+'users';

    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json;charset=UTF-8' })
    };

    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) { }

    readUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched users')),
                catchError(this.handleError<User[]>('readUsers', "Could not get user list.", []))
            );
    }

    createUser(user: User): Observable<User> {
        // TODO validation
        return this.http.post<User>(this.baseUrl, user, this.httpOptions)
            .pipe(
                tap((newUser: User) => this.info('createUser', `Added user: "${newUser.user}"`)),
                catchError(this.handleError<User>('createUser',`Could not add "${user.user}"` ))
            );
    }

    updateUser(user: User): Observable<User> {
        // TODO validation
        return this.http.put<User>(this.baseUrl, user, this.httpOptions)
            .pipe(
                tap((updatedUser: User) => this.info('updateUser', `Updated user: "${updatedUser.user}"`)),
                catchError(this.handleError<User>('updateUser',`Could not update "${user.user}"` ))
            );
    }

    deleteUser(user: User): Observable<User> {
        // TODO validation
        return this.http.delete<User>(`${this.baseUrl}/${user.user}`)
            .pipe(
                tap(_ => this.info('deleteUser', `Removed user: ${user.user}`)),
                catchError(this.handleError<User>('deleteUser',`Could not remove "${user.user}"`,
                                                  user)) // return user, meaning it wasn't deleted
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
