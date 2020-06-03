import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../environments/environment';

import { User } from './user';
import { Message } from './message';
import { MessageService } from './message.service';
import { Service } from './service';

@Injectable({
  providedIn: 'root'
})
export class UserService extends Service {
    
    constructor(
        private http: HttpClient,
        messageService: MessageService) {
        super(messageService, environment.baseUrl+'users');
    }

    whoAmI(): Observable<User> {
        if (environment.production) {
            return this.http.get<User>(environment.baseUrl + "user")
                .pipe(
                    tap(user => this.log(`logged-in user: ${user.user}`)),
                    catchError(this.handleError<User>(
                        'access user information', "Could not get logged-in user."))
                );
        } else { // development environment server doesn't have user auth, so fake it
            return of({ user: "admin" } as User);
        }
    }

    setPassword(password: string): Observable<Message> {
        return this.http.put<Message>(environment.baseUrl + "user", { password: password }, this.httpOptions)
            .pipe(
                tap(_ => this.info('setPassword', `Password set`)),
                catchError(this.handleError<Message>('set your password',""))
            );
    }

    readUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.baseUrl)
            .pipe(
                tap(_ => this.log('fetched users')),
                catchError(this.handleError<User[]>('list users', "Could not get user list.", []))
            );
    }

    createUser(user: User): Observable<User> {
        // TODO validation
        return this.http.post<User>(this.baseUrl, user, this.httpOptions)
            .pipe(
                tap((newUser: User) => this.info('createUser', `Added user: "${newUser.user}"`)),
                catchError(this.handleError<User>('create users',`Could not add "${user.user}"` ))
            );
    }

    updateUser(user: User): Observable<User> {
        // TODO validation
        return this.http.put<User>(this.baseUrl, user, this.httpOptions)
            .pipe(
                tap((updatedUser: User) => this.info(
                    'updateUser', `Updated user: "${updatedUser.user}"`)),
                catchError(this.handleError<User>(
                    'update users',`Could not update "${user.user}"` ))
            );
    }

    deleteUser(user: User): Observable<User> {
        // TODO validation
        return this.http.delete<User>(`${this.baseUrl}/${user.user}`)
            .pipe(
                tap(_ => this.info('deleteUser', `Removed user: ${user.user}`)),
                catchError(this.handleError<User>(
                    'delete users',`Could not remove "${user.user}"`,
                    user)) // return user, meaning it wasn't deleted
            );
    }

}
