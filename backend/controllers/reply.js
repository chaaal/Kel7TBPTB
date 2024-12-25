const Reply = require('../models/Reply');
const KPRequest = require('../models/KPRequest');
const Group = require('../models/Group');
const Member = require('../models/Member');
const GroupMember = require('../models/GroupMember');
const path = require('path');
const fs = require('fs');

// Get all replies
const getReplies = async (req, res) => {
    try {
        const replies = await Reply.findAll({
            include: [
                {
                    model: KPRequest,
                    where: {
                        status: "Accepted"
                    },
                    include: [
                        {
                            model: Group,
                            include: [
                                {
                                    model: Member, // Langsung ke Member tanpa menyebut GroupMember
                                    through: { attributes: [] } // Hilangkan atribut dari tabel pivot
                                }
                            ]
                        }
                    ]
                }
            ],
            order: [['createdAt', 'DESC']]
        });

        return res.status(200).json({
            success: true,
            data: replies
        });
    } catch (error) {
        console.error('Error fetching replies:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to fetch replies',
            error: error.message
        });
    }
};

const getUnreplies = async (req, res) => {
    try {
        const replies = await Reply.findAll({
            include: [
                {
                    model: KPRequest,
                    where: {
                        status: "Approved"
                    },
                    include: [
                        {
                            model: Group,
                            include: [
                                {
                                    model: Member, // Langsung ke Member tanpa menyebut GroupMember
                                    through: { attributes: [] } // Hilangkan atribut dari tabel pivot
                                }
                            ]
                        }
                    ]
                }
            ],
            order: [['createdAt', 'DESC']]
        });

        return res.status(200).json({
            success: true,
            data: replies
        });
    } catch (error) {
        console.error('Error fetching replies:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to fetch replies',
            error: error.message
        });
    }
};

// Get specific reply
const getReply = async (req, res) => {
    try {
        const { id } = req.params;

        const reply = await Reply.findOne({
            where: { id },
            include: [{
                model: KPRequest,
                include: [{
                    model: Group,
                    include: [{
                        model: Member,
                        through: GroupMember,
                        attributes: ['id', 'name', 'nim', 'email', 'phoneNumber']
                    }]
                }]
            }]
        });

        if (!reply) {
            return res.status(404).json({
                success: false,
                message: 'Reply not found'
            });
        }

        return res.status(200).json({
            success: true,
            data: reply
        });
    } catch (error) {
        console.error('Error fetching reply:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to fetch reply',
            error: error.message
        });
    }
};

// Download reply document
const downloadReply = async (req, res) => {
    try {
        const { id } = req.params;

        const reply = await Reply.findOne({
            where: { id },
            include: [{
                model: KPRequest,
                include: [{
                    model: Group,
                    attributes: ['name']
                }]
            }]
        });

        if (!reply || !reply.responseLetterUrl) {
            return res.status(404).json({
                success: false,
                message: 'Reply document not found'
            });
        }

        const filePath = path.join(__dirname, '..', 'storage', 'replies', reply.responseLetterUrl);

        if (!fs.existsSync(filePath)) {
            return res.status(404).json({
                success: false,
                message: 'Reply file not found on server'
            });
        }

        const fileName = `Reply_Letter_${reply.KPRequest.Group.name}_${id}${path.extname(reply.responseLetterUrl)}`;
        
        res.download(filePath, fileName, (err) => {
            if (err) {
                console.error('Error downloading reply:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error downloading reply document',
                    error: err.message
                });
            }
        });
    } catch (error) {
        console.error('Error downloading reply:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to download reply document',
            error: error.message
        });
    }
};

// Add new reply with file upload
const addReply = async (req, res) => {
    try {
        const { id } = req.params; // KP Request ID
        
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'No reply document uploaded'
            });
        }

        // Check if KP request exists with group details
        const kpRequest = await KPRequest.findOne({
            where: { id },
            include: [{
                model: Group,
                include: [{
                    model: Member,
                    through: GroupMember
                }]
            }]
        });

        if (!kpRequest) {
            fs.unlinkSync(req.file.path);
            return res.status(404).json({
                success: false,
                message: 'KP Request not found'
            });
        }

        // Check if reply already exists
        const existingReply = await Reply.findOne({ where: { idKP: id }});
        if (existingReply) {
            fs.unlinkSync(req.file.path);
            return res.status(400).json({
                success: false,
                message: 'Reply already exists for this KP request'
            });
        }

        // Create reply record
        const reply = await Reply.create({
            idKP: id,
            responseLetterUrl: req.file.filename
        });

        // Update KP request status
        await kpRequest.update({ status: 'Accepted' });

        return res.status(201).json({
            success: true,
            message: 'Reply added successfully',
            data: {
                ...reply.toJSON(),
                kpRequest: kpRequest.toJSON()
            }
        });

    } catch (error) {
        if (req.file) {
            fs.unlinkSync(req.file.path);
        }
        
        console.error('Error adding reply:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to add reply',
            error: error.message
        });
    }
};

module.exports = {
    getReplies,
    getReply,
    downloadReply,
    addReply,
    getUnreplies
};