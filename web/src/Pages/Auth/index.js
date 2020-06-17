import React from 'react';
import {coreRequest} from "../../Utilities/Rest";
import {useAuth} from "../../Utilities/Auth";
import useStyles from "./style";

//MUI components
import Input from "@material-ui/core/Input";
import InputAdornment from "@material-ui/core/InputAdornment";
import IconButton from "@material-ui/core/IconButton";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";
import ListItemSecondaryAction from "@material-ui/core/ListItemSecondaryAction";
import ListItemText from "@material-ui/core/ListItemText";
import Checkbox from "@material-ui/core/Checkbox";
import Paper from "@material-ui/core/Paper";

//MUI Icons
import {Visibility, VisibilityOff} from "@material-ui/icons";


export default function Auth({
                                 onComplete = () => {
                                 },
                                 ...props
                             }) {
    const [showPassword, setShowPassword] = React.useState(false);
    const [error, setError] = React.useState(null);
    const [data, setData] = React.useState({email: null, password: null, remember: false});
    const {setUser} = useAuth();
    const classes = useStyles();

    function handleLogin() {
        if (!data.email || !data.password) {
            setError('Credential cannot be empty');
            return;
        }

        coreRequest().post('login')
            .send(data)
            .then(response => {
                console.log(response);
            })
            .catch(error => {
                console.error(error)
            });
    }

    function handleChangePassword(event) {
        event.persist();
        setData(last => ({...last, password: event.target.value || null}))
    }

    function handleChangeEmail(event) {
        event.persist();
        setData(last => ({...last, email: event.target.value || null}))
    }

    function handleChangeShowPassword(event) {
        setShowPassword(item => !item);
    }

    function handleRememberMe(event) {
        event.persist();
        setData(last => ({...last, remember: event.target.checked || false}));
    }

    return (
        <Paper className={classes.paper}>
            <List>
                {error && <ListItem>
                    <Typography color={'error'} variant={'body2'}>
                        {error}
                    </Typography>
                </ListItem>}
                <ListItem>
                    <Input
                        placeholder={'Email'}
                        fullWidth
                        required
                        autoComplete={'email'}
                        onChange={handleChangeEmail}
                        value={data.email || ''}
                    />
                </ListItem>
                <ListItem>
                    <Input
                        id="standard-adornment-password"
                        type={showPassword ? 'text' : 'password'}
                        value={data.password || ''}
                        placeholder={'Password'}
                        onChange={handleChangePassword}
                        autoComplete={'password'}
                        fullWidth
                        required
                        endAdornment={
                            <InputAdornment position="end">
                                <IconButton
                                    aria-label="toggle password visibility"
                                    onClick={handleChangeShowPassword}
                                >
                                    {showPassword ? <Visibility/> : <VisibilityOff/>}
                                </IconButton>
                            </InputAdornment>
                        }
                    />
                </ListItem>
                <ListItem>
                    <ListItemText primary={'Запам\'ятати мене'}/>
                    <ListItemSecondaryAction>
                        <Checkbox checked={data.remember} onChange={handleRememberMe}/>
                    </ListItemSecondaryAction>
                </ListItem>
                <ListItem>
                    <Button fullWidth onClick={handleLogin}>
                        Увійти
                    </Button>
                </ListItem>
            </List>
        </Paper>
    );
}