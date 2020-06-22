import { makeStyles } from '@material-ui/core/styles';
import {blue,red} from "@material-ui/core/colors";

export const useStyles = makeStyles({
    table: {
        minWidth: 650,
    },
    activeTable: {
        backgroundColor: blue[100],
    },
    error:{
        color: red[700],
    }
});